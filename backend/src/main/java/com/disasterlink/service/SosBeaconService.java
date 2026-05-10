package com.disasterlink.service;

import com.disasterlink.dto.SosBeaconRequest;
import com.disasterlink.dto.SosBeaconResponse;
import com.disasterlink.model.SosBeacon;
import com.disasterlink.model.Volunteer;
import com.disasterlink.repository.SosBeaconRepository;
import com.disasterlink.websocket.WebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SosBeaconService {

    private final SosBeaconRepository sosBeaconRepository;
    private final AiTriageService aiTriageService;
    private final VolunteerAssignmentService assignmentService;
    private final WebSocketBroadcaster webSocketBroadcaster;
    private final MessageBrokerService messageBrokerService;

    /**
     * Core flow when a new SOS is received:
     * 1. Save beacon immediately (any delay = lives at risk)
     * 2. Broadcast raw beacon to all connected district officer dashboards
     * 3. Async: run AI triage, then update + broadcast enriched beacon
     * 4. Async: attempt auto-assignment of nearest skilled volunteer
     */
    @Transactional
    public SosBeaconResponse createSosBeacon(SosBeaconRequest request) {
        // Step 1: Save immediately
        SosBeacon beacon = SosBeacon.builder()
                .victimName(request.getVictimName())
                .contactPhone(request.getContactPhone())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationDescription(request.getLocationDescription())
                .disasterType(request.getDisasterType())
                .situationDetails(request.getSituationDetails())
                .affectedCount(request.getAffectedCount())
                .source(SosBeacon.SosSource.APP)
                .status(SosBeacon.SosStatus.ACTIVE)
                .triagePriority(SosBeacon.TriagePriority.MEDIUM)  // temp until AI runs
                .triageScore(50)
                .build();

        beacon = sosBeaconRepository.save(beacon);
        log.info("SOS beacon #{} saved - lat={}, lng={}", beacon.getId(),
                beacon.getLatitude(), beacon.getLongitude());

        // Step 2: Broadcast raw to dashboard immediately
        webSocketBroadcaster.broadcastNewSos(beacon);

        // Step 3 & 4: Run AI triage + assignment asynchronously
        final Long beaconId = beacon.getId();
        runAsyncTriageAndAssignment(beaconId);

        return SosBeaconResponse.from(beacon);
    }

    /**
     * Handle SMS-sourced SOS (from Twilio webhook).
     * Parses: "SOS FLOOD PUNE 50 PEOPLE TRAPPED"
     */
    @Transactional
    public SosBeaconResponse createSosFromSms(String fromPhone, String messageBody,
                                               Double lat, Double lng) {
        String parsed = messageBody.toUpperCase().trim();
        String disasterType = extractDisasterType(parsed);
        int affectedCount = extractAffectedCount(parsed);

        SosBeacon beacon = SosBeacon.builder()
                .contactPhone(fromPhone)
                .latitude(lat != null ? lat : 18.5204)   // fallback Pune coords
                .longitude(lng != null ? lng : 73.8567)
                .disasterType(disasterType)
                .situationDetails("SMS Alert: " + messageBody)
                .affectedCount(affectedCount)
                .source(SosBeacon.SosSource.SMS)
                .rawSmsText(messageBody)
                .status(SosBeacon.SosStatus.ACTIVE)
                .triagePriority(SosBeacon.TriagePriority.MEDIUM)
                .triageScore(50)
                .build();

        beacon = sosBeaconRepository.save(beacon);
        webSocketBroadcaster.broadcastNewSos(beacon);
        final Long beaconId = beacon.getId();
        runAsyncTriageAndAssignment(beaconId);

        return SosBeaconResponse.from(beacon);
    }

    @Async
    public void runAsyncTriageAndAssignment(Long beaconId) {
        try {
            SosBeacon beacon = sosBeaconRepository.findById(beaconId).orElseThrow();

            // AI Triage
            AiTriageService.TriageResult triage = aiTriageService.analyseSos(beacon);
            beacon.setTriagePriority(triage.priority());
            beacon.setTriageScore(triage.score());
            beacon.setTriageReason(triage.reason());
            beacon = sosBeaconRepository.save(beacon);

            log.info("SOS #{} triaged: {} (score={})", beaconId, triage.priority(), triage.score());
            webSocketBroadcaster.broadcastSosUpdate(beacon);

            // Publish to message broker (Redis Pub/Sub, Kafka-compatible interface)
            messageBrokerService.publish("sos-triage-complete", Map.of(
                "beaconId", beaconId,
                "priority", triage.priority().name(),
                "score", triage.score()
            ));

            // Auto-assignment for CRITICAL and HIGH
            if (triage.priority() == SosBeacon.TriagePriority.CRITICAL ||
                triage.priority() == SosBeacon.TriagePriority.HIGH) {
                assignmentService.autoAssignVolunteer(beacon);
            }

        } catch (Exception e) {
            log.error("Async triage/assignment failed for beacon {}: {}", beaconId, e.getMessage());
        }
    }

    public List<SosBeaconResponse> getActiveBeacons() {
        return sosBeaconRepository
                .findByStatusOrderByTriageScoreDesc(SosBeacon.SosStatus.ACTIVE)
                .stream()
                .map(SosBeaconResponse::from)
                .toList();
    }

    public List<SosBeaconResponse> getAllBeacons() {
        return sosBeaconRepository.findAll()
                .stream()
                .map(SosBeaconResponse::from)
                .toList();
    }

    @Transactional
    public SosBeaconResponse updateStatus(Long beaconId, SosBeacon.SosStatus newStatus) {
        SosBeacon beacon = sosBeaconRepository.findById(beaconId)
                .orElseThrow(() -> new RuntimeException("Beacon not found: " + beaconId));

        beacon.setStatus(newStatus);
        if (newStatus == SosBeacon.SosStatus.RESOLVED) {
            beacon.setResolvedAt(LocalDateTime.now());
        }

        beacon = sosBeaconRepository.save(beacon);
        webSocketBroadcaster.broadcastSosUpdate(beacon);
        return SosBeaconResponse.from(beacon);
    }

    // --- SMS parsing helpers ---

    private String extractDisasterType(String text) {
        if (text.contains("FLOOD")) return "FLOOD";
        if (text.contains("EARTHQUAKE") || text.contains("QUAKE")) return "EARTHQUAKE";
        if (text.contains("FIRE")) return "FIRE";
        if (text.contains("CYCLONE") || text.contains("STORM")) return "CYCLONE";
        if (text.contains("LANDSLIDE")) return "LANDSLIDE";
        return "OTHER";
    }

    private int extractAffectedCount(String text) {
        // Look for patterns like "50 PEOPLE" or "PEOPLE 50"
        String[] words = text.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            try {
                int num = Integer.parseInt(words[i]);
                if (num > 0 && num < 100000) return num;
            } catch (NumberFormatException ignored) {}
        }
        return 1;
    }
}
