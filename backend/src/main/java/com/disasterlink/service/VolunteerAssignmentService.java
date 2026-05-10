package com.disasterlink.service;

import com.disasterlink.model.SosBeacon;
import com.disasterlink.model.Volunteer;
import com.disasterlink.repository.SosBeaconRepository;
import com.disasterlink.repository.VolunteerRepository;
import com.disasterlink.websocket.WebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Volunteer Assignment Service
 *
 * Auto-assigns the nearest available volunteer with relevant skills.
 * Uses Haversine formula for accurate distance calculation.
 *
 * Assignment priority:
 *  1. Skill match (MEDICAL for injury, BOAT_OPERATOR for flood, etc.)
 *  2. Distance (nearest first)
 *  3. Availability
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VolunteerAssignmentService {

    private final VolunteerRepository volunteerRepository;
    private final SosBeaconRepository sosBeaconRepository;
    private final WebSocketBroadcaster webSocketBroadcaster;
    private final SmsService smsService;

    // Search radius in km - expand if no match found
    private static final double INITIAL_RADIUS_KM = 10.0;
    private static final double MAX_RADIUS_KM = 50.0;

    @Transactional
    public Optional<Volunteer> autoAssignVolunteer(SosBeacon beacon) {
        String requiredSkill = mapDisasterToSkill(beacon.getDisasterType(),
                beacon.getSituationDetails());

        Optional<Volunteer> bestVolunteer = findBestVolunteer(
                beacon.getLatitude(), beacon.getLongitude(), requiredSkill);

        if (bestVolunteer.isEmpty()) {
            log.warn("No volunteer found for beacon #{} within {}km", beacon.getId(), MAX_RADIUS_KM);
            return Optional.empty();
        }

        Volunteer volunteer = bestVolunteer.get();
        assignVolunteerToBeacon(volunteer, beacon);

        log.info("Volunteer #{} ({}) assigned to beacon #{}",
                volunteer.getId(), volunteer.getName(), beacon.getId());

        // Notify via SMS
        smsService.notifyVolunteerAssignment(volunteer, beacon);

        return Optional.of(volunteer);
    }

    @Transactional
    public void assignVolunteerToBeacon(Volunteer volunteer, SosBeacon beacon) {
        // Update volunteer
        volunteer.setStatus(Volunteer.VolunteerStatus.ASSIGNED);
        volunteer.setCurrentSosId(beacon.getId());
        volunteer.setAssignedAt(LocalDateTime.now());
        volunteerRepository.save(volunteer);

        // Update beacon
        beacon.setAssignedVolunteerId(volunteer.getId());
        beacon.setAssignedAt(LocalDateTime.now());
        beacon.setStatus(SosBeacon.SosStatus.ASSIGNED);
        sosBeaconRepository.save(beacon);

        // Broadcast assignment to dashboard
        webSocketBroadcaster.broadcastAssignment(beacon, volunteer);
    }

    @Transactional
    public void releaseVolunteer(Long volunteerId) {
        volunteerRepository.findById(volunteerId).ifPresent(v -> {
            v.setStatus(Volunteer.VolunteerStatus.AVAILABLE);
            v.setCurrentSosId(null);
            v.setAssignedAt(null);
            v.setTotalAssignments(
                    v.getTotalAssignments() != null ? v.getTotalAssignments() + 1 : 1);
            volunteerRepository.save(v);
        });
    }

    private Optional<Volunteer> findBestVolunteer(double lat, double lng, String skill) {
        double radius = INITIAL_RADIUS_KM;

        while (radius <= MAX_RADIUS_KM) {
            // Bounding box (approx 1 deg lat/lng ≈ 111km)
            double delta = radius / 111.0;
            List<Volunteer> candidates;

            if (skill != null) {
                candidates = volunteerRepository.findAvailableBySkill(skill);
            } else {
                candidates = volunteerRepository.findAvailableInBoundingBox(
                        lat - delta, lat + delta, lng - delta, lng + delta);
            }

            // Filter by actual radius and sort by distance
            Optional<Volunteer> best = candidates.stream()
                    .filter(v -> v.getCurrentLatitude() != null)
                    .filter(v -> haversineKm(lat, lng,
                            v.getCurrentLatitude(), v.getCurrentLongitude()) <= radius)
                    .min(Comparator.comparingDouble(v ->
                            haversineKm(lat, lng,
                                    v.getCurrentLatitude(), v.getCurrentLongitude())));

            if (best.isPresent()) return best;

            // Widen search
            radius += 10.0;
            log.debug("No volunteer in {}km, expanding to {}km", radius - 10, radius);
        }

        return Optional.empty();
    }

    /**
     * Haversine formula - great-circle distance between two lat/lng points.
     * Used to find nearest volunteer accurately.
     */
    public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Maps disaster type and situation keywords to required volunteer skill.
     */
    private String mapDisasterToSkill(String disasterType, String details) {
        String d = disasterType != null ? disasterType.toUpperCase() : "";
        String s = details != null ? details.toLowerCase() : "";

        if (s.contains("injur") || s.contains("bleed") || s.contains("unconscious")
                || s.contains("medical")) return "MEDICAL";
        if (d.equals("FLOOD") || s.contains("drown") || s.contains("boat")) return "BOAT_OPERATOR";
        if (d.equals("EARTHQUAKE") || s.contains("collapse") || s.contains("trapped"))
            return "STRUCTURAL_ENGINEER";
        if (d.equals("FIRE")) return "FIREFIGHTER";
        return "RESCUE"; // default
    }
}
