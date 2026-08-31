package com.disasterlink.service;

import com.disasterlink.dto.AssignVolunteerRequest;
import com.disasterlink.dto.SosRequest;
import com.disasterlink.dto.SosResponse;
import com.disasterlink.dto.SosStatusHistoryResponse;
import com.disasterlink.dto.StatusUpdateRequest;
import com.disasterlink.dto.WebSocketNotification;
import com.disasterlink.entity.ReportStatus;
import com.disasterlink.entity.Role;
import com.disasterlink.entity.SosBeacon;
import com.disasterlink.entity.SosStatusHistory;
import com.disasterlink.entity.User;
import com.disasterlink.entity.VolunteerStatus;
import com.disasterlink.exception.InvalidStatusTransitionException;
import com.disasterlink.exception.ResourceNotFoundException;
import com.disasterlink.exception.UnauthorizedException;
import com.disasterlink.repository.SosBeaconRepository;
import com.disasterlink.repository.SosStatusHistoryRepository;
import com.disasterlink.repository.UserRepository;
import com.disasterlink.service.GeminiTriageService.TriageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Core business logic for SOS beacons.
 *
 * Flow for a new submission:
 *  1. Validate + save the beacon (PENDING, score=0)
 *  2. Run Gemini (or rule-based) triage → update score + label
 *  3. Broadcast the beacon over WebSocket
 *  4. Async ntfy push if CRITICAL or HIGH
 *
 * Status transition rules (strictly enforced):
 *   PENDING → ASSIGNED → EN_ROUTE → ARRIVED → IN_PROGRESS → RESOLVED
 */
@Service
@Transactional
public class SosBeaconService {

    private final SosBeaconRepository beaconRepository;
    private final UserRepository userRepository;
    private final SosStatusHistoryRepository historyRepository;
    private final GeminiTriageService geminiTriageService;
    private final NtfyNotificationService ntfyService;
    private final WebSocketNotificationService wsService;

    /**
     * Allowed status transitions map.
     * Key = current status, Value = set of statuses that key can transition TO.
     */
    private static final Map<ReportStatus, Set<ReportStatus>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(ReportStatus.class);
        ALLOWED_TRANSITIONS.put(ReportStatus.PENDING,      Set.of(ReportStatus.ASSIGNED, ReportStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(ReportStatus.ASSIGNED,     Set.of(ReportStatus.EN_ROUTE));
        ALLOWED_TRANSITIONS.put(ReportStatus.EN_ROUTE,     Set.of(ReportStatus.ARRIVED));
        ALLOWED_TRANSITIONS.put(ReportStatus.ARRIVED,      Set.of(ReportStatus.IN_PROGRESS));
        ALLOWED_TRANSITIONS.put(ReportStatus.IN_PROGRESS,  Set.of(ReportStatus.RESOLVED));
        ALLOWED_TRANSITIONS.put(ReportStatus.RESOLVED,     Set.of());
        ALLOWED_TRANSITIONS.put(ReportStatus.CANCELLED,    Set.of());
    }

    public SosBeaconService(SosBeaconRepository beaconRepository,
                             UserRepository userRepository,
                             SosStatusHistoryRepository historyRepository,
                             GeminiTriageService geminiTriageService,
                             NtfyNotificationService ntfyService,
                             WebSocketNotificationService wsService) {
        this.beaconRepository = beaconRepository;
        this.userRepository   = userRepository;
        this.historyRepository = historyRepository;
        this.geminiTriageService = geminiTriageService;
        this.ntfyService      = ntfyService;
        this.wsService        = wsService;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SosResponse> getAllBeacons() {
        return beaconRepository.findAllByOrderByUrgencyScoreDescCreatedAtDesc()
                .stream()
                .map(SosResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SosResponse getBeaconById(Long id, String username, Role role) {
        SosBeacon beacon = findBeaconOrThrow(id);
        if (role == Role.VICTIM && !beacon.getReporter().getUsername().equals(username)) {
            throw new UnauthorizedException("You can only view your own SOS beacons");
        }
        return SosResponse.fromEntity(beacon);
    }

    /** Returns only the beacons submitted by the requesting victim. */
    @Transactional(readOnly = true)
    public List<SosResponse> getMyBeacons(String username) {
        return beaconRepository.findAllByReporterUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(SosResponse::fromEntity)
                .toList();
    }

    /** Returns beacons assigned to the requesting volunteer. */
    @Transactional(readOnly = true)
    public List<SosResponse> getAssignedBeacons(String username) {
        return beaconRepository.findAllByAssignedVolunteerUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(SosResponse::fromEntity)
                .toList();
    }

    /** Returns the full status-change timeline for a given SOS. */
    @Transactional(readOnly = true)
    public List<SosStatusHistoryResponse> getHistory(Long sosId) {
        findBeaconOrThrow(sosId); // validates SOS exists
        return historyRepository.findAllBySosIdOrderByChangedAtAsc(sosId)
                .stream()
                .map(h -> new SosStatusHistoryResponse(
                        h.getId(),
                        h.getSos().getId(),
                        h.getOldStatus(),
                        h.getNewStatus(),
                        h.getChangedBy(),
                        h.getChangedAt()))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    /**
     * Submits a new SOS beacon.
     * Runs triage synchronously (≤ 10 s), then broadcasts + notifies async.
     */
    public SosResponse createBeacon(SosRequest request, String reporterUsername) {
        User reporter = findUserOrThrow(reporterUsername);

        SosBeacon beacon = new SosBeacon();
        beacon.setDescription(request.getDescription());
        beacon.setDisasterType(request.getDisasterType());
        beacon.setLatitude(request.getLatitude());
        beacon.setLongitude(request.getLongitude());
        beacon.setAddress(request.getAddress());
        beacon.setStatus(ReportStatus.PENDING);
        beacon.setReporter(reporter);

        // Run Gemini AI or rule-based triage
        TriageResult triage = geminiTriageService.triage(request.getDescription(), request.getDisasterType());
        beacon.setUrgencyScore(triage.score());
        beacon.setUrgencyLabel(triage.label());
        beacon.setTriageNote(triage.note());

        SosBeacon saved = beaconRepository.save(beacon);

        // Record creation in history
        saveHistory(saved, null, ReportStatus.PENDING.name(), reporterUsername);

        SosResponse response = SosResponse.fromEntity(saved);

        // Broadcast to general feed (backward compat) + notify officers
        wsService.broadcast(response, "NEW");
        wsService.notifyNewSos(new WebSocketNotification(saved.getId(), "NEW_SOS",
                "New " + saved.getUrgencyLabel() + " SOS submitted by " + reporterUsername));

        // Async ntfy push for high-urgency
        ntfyService.notifyIfUrgent(saved.getId(), saved.getDisasterType().name(),
                saved.getUrgencyLabel(), saved.getAddress());

        return response;
    }

    /**
     * Officer assigns a volunteer to an open beacon.
     * - Volunteer must be AVAILABLE
     * - SOS must be PENDING
     * - Status advances to ASSIGNED
     * - Volunteer status → BUSY
     */
    public SosResponse assignVolunteer(Long beaconId, AssignVolunteerRequest request, String officerUsername) {
        SosBeacon beacon = findBeaconOrThrow(beaconId);
        User volunteer = userRepository.findById(request.getVolunteerId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found: " + request.getVolunteerId()));

        if (volunteer.getRole() != Role.VOLUNTEER) {
            throw new UnauthorizedException("User " + request.getVolunteerId() + " is not a volunteer");
        }

        if (volunteer.getVolunteerStatus() != VolunteerStatus.AVAILABLE) {
            throw new UnauthorizedException("Volunteer is not available (current status: "
                    + volunteer.getVolunteerStatus() + ")");
        }

        if (beacon.getStatus() != ReportStatus.PENDING) {
            throw new UnauthorizedException(
                    "Can only assign a volunteer to a PENDING SOS (current status: " + beacon.getStatus() + ")");
        }

        String oldStatus = beacon.getStatus().name();
        beacon.setAssignedVolunteer(volunteer);
        beacon.setStatus(ReportStatus.ASSIGNED);
        volunteer.setVolunteerStatus(VolunteerStatus.BUSY);

        userRepository.save(volunteer);
        SosBeacon saved = beaconRepository.save(beacon);
        saveHistory(saved, oldStatus, ReportStatus.ASSIGNED.name(), officerUsername);

        SosResponse response = SosResponse.fromEntity(saved);
        wsService.broadcast(response, "ASSIGNED");

        // Notify the specific volunteer
        wsService.notifyVolunteer(volunteer.getId(),
                new WebSocketNotification(saved.getId(), "VOLUNTEER_ASSIGNED",
                        "You have been assigned a new SOS task"));

        // Notify the victim
        if (saved.getReporter() != null) {
            wsService.notifyVictim(saved.getReporter().getId(),
                    new WebSocketNotification(saved.getId(), "VOLUNTEER_ASSIGNED",
                            "A volunteer has been assigned to your SOS"));
        }

        return response;
    }

    /**
     * Officer reassigns a different volunteer to an already-assigned beacon.
     * - Old volunteer → AVAILABLE
     * - New volunteer must be AVAILABLE → BUSY
     * - Status stays ASSIGNED
     */
    public SosResponse reassignVolunteer(Long beaconId, AssignVolunteerRequest request, String officerUsername) {
        SosBeacon beacon = findBeaconOrThrow(beaconId);
        User newVolunteer = userRepository.findById(request.getVolunteerId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found: " + request.getVolunteerId()));

        if (newVolunteer.getRole() != Role.VOLUNTEER) {
            throw new UnauthorizedException("User " + request.getVolunteerId() + " is not a volunteer");
        }

        if (newVolunteer.getVolunteerStatus() != VolunteerStatus.AVAILABLE) {
            throw new UnauthorizedException("New volunteer is not available (status: "
                    + newVolunteer.getVolunteerStatus() + ")");
        }

        // Free up the old volunteer
        User oldVolunteer = beacon.getAssignedVolunteer();
        if (oldVolunteer != null) {
            oldVolunteer.setVolunteerStatus(VolunteerStatus.AVAILABLE);
            userRepository.save(oldVolunteer);
        }

        String oldStatus = beacon.getStatus().name();
        beacon.setAssignedVolunteer(newVolunteer);
        beacon.setStatus(ReportStatus.ASSIGNED);
        newVolunteer.setVolunteerStatus(VolunteerStatus.BUSY);

        userRepository.save(newVolunteer);
        SosBeacon saved = beaconRepository.save(beacon);

        String note = "Reassigned from " +
                (oldVolunteer != null ? oldVolunteer.getUsername() : "none") +
                " to " + newVolunteer.getUsername();
        saveHistory(saved, oldStatus, ReportStatus.ASSIGNED.name(), officerUsername + " (reassign: " + note + ")");

        SosResponse response = SosResponse.fromEntity(saved);
        wsService.broadcast(response, "REASSIGNED");

        // Notify new volunteer
        wsService.notifyVolunteer(newVolunteer.getId(),
                new WebSocketNotification(saved.getId(), "VOLUNTEER_ASSIGNED",
                        "You have been assigned a new SOS task"));

        // Notify victim
        if (saved.getReporter() != null) {
            wsService.notifyVictim(saved.getReporter().getId(),
                    new WebSocketNotification(saved.getId(), "VOLUNTEER_REASSIGNED",
                            "Your SOS has been reassigned to a new volunteer"));
        }

        return response;
    }

    /**
     * Updates the lifecycle status of a beacon with strict transition validation.
     * Officers can set any valid next status; volunteers can only update their own beacons.
     * When RESOLVED: volunteer status → AVAILABLE.
     */
    public SosResponse updateStatus(Long beaconId, StatusUpdateRequest request,
                                     String username, Role role) {
        SosBeacon beacon = findBeaconOrThrow(beaconId);

        // Volunteers can only update beacons assigned to them
        if (role == Role.VOLUNTEER &&
                (beacon.getAssignedVolunteer() == null ||
                 !beacon.getAssignedVolunteer().getUsername().equals(username))) {
            throw new UnauthorizedException("You can only update beacons assigned to you");
        }

        ReportStatus currentStatus = beacon.getStatus();
        ReportStatus newStatus = request.getStatus();

        // Validate transition is allowed
        Set<ReportStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStatusTransitionException(currentStatus.name(), newStatus.name());
        }

        beacon.setStatus(newStatus);
        SosBeacon saved = beaconRepository.save(beacon);

        saveHistory(saved, currentStatus.name(), newStatus.name(), username);

        // When RESOLVED, free the volunteer
        if (newStatus == ReportStatus.RESOLVED && saved.getAssignedVolunteer() != null) {
            User volunteer = saved.getAssignedVolunteer();
            volunteer.setVolunteerStatus(VolunteerStatus.AVAILABLE);
            userRepository.save(volunteer);
        }

        SosResponse response = SosResponse.fromEntity(saved);
        wsService.broadcast(response, "STATUS_UPDATE");

        // Notify the victim
        if (saved.getReporter() != null) {
            wsService.notifyVictim(saved.getReporter().getId(),
                    new WebSocketNotification(saved.getId(), "STATUS_UPDATED",
                            "Your SOS status changed to: " + newStatus.name()));
        }

        // Notify all officers if resolved
        if (newStatus == ReportStatus.RESOLVED) {
            wsService.notifySosResolved(new WebSocketNotification(saved.getId(), "SOS_RESOLVED",
                    "SOS #" + saved.getId() + " has been resolved by " + username));
        }

        return response;
    }

    /** Victims can cancel their own pending beacons. Officers can delete any. Volunteers cannot delete. */
    public void deleteBeacon(Long beaconId, String username, Role role) {
        SosBeacon beacon = findBeaconOrThrow(beaconId);

        if (role == Role.VOLUNTEER) {
            throw new UnauthorizedException("Volunteers cannot cancel SOS beacons");
        }

        if (role == Role.VICTIM) {
            if (!beacon.getReporter().getUsername().equals(username)) {
                throw new UnauthorizedException("You can only cancel your own SOS beacons");
            }
            if (beacon.getStatus() != ReportStatus.PENDING) {
                throw new UnauthorizedException("You can only cancel pending SOS beacons");
            }
        }

        beaconRepository.delete(beacon);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void saveHistory(SosBeacon sos, String oldStatus, String newStatus, String changedBy) {
        SosStatusHistory history = new SosStatusHistory(sos, oldStatus, newStatus, changedBy);
        historyRepository.save(history);
    }

    private SosBeacon findBeaconOrThrow(Long id) {
        return beaconRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SOS beacon not found: " + id));
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
