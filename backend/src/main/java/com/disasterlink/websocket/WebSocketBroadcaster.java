package com.disasterlink.websocket;

import com.disasterlink.dto.SosBeaconResponse;
import com.disasterlink.model.SosBeacon;
import com.disasterlink.model.Volunteer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * WebSocket Broadcaster
 *
 * Uses STOMP over WebSocket to push real-time events to:
 *   - /topic/sos        → all district officer dashboards
 *   - /topic/assignments → volunteer app instances
 *   - /topic/zones      → map updates
 *
 * Flow: SOS created → saved to DB → broadcastNewSos() → all connected
 * dashboards instantly see new pin on the Leaflet.js map.
 *
 * Latency target: < 500ms from SOS submission to map pin appearance.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastNewSos(SosBeacon beacon) {
        SosBeaconResponse payload = SosBeaconResponse.from(beacon);
        messagingTemplate.convertAndSend("/topic/sos", Map.of(
            "event", "SOS_CREATED",
            "data", payload
        ));
        log.debug("Broadcast SOS_CREATED for beacon #{}", beacon.getId());
    }

    public void broadcastSosUpdate(SosBeacon beacon) {
        SosBeaconResponse payload = SosBeaconResponse.from(beacon);
        messagingTemplate.convertAndSend("/topic/sos", Map.of(
            "event", "SOS_UPDATED",
            "data", payload
        ));
        log.debug("Broadcast SOS_UPDATED for beacon #{}", beacon.getId());
    }

    public void broadcastAssignment(SosBeacon beacon, Volunteer volunteer) {
        messagingTemplate.convertAndSend("/topic/assignments", Map.of(
            "event", "VOLUNTEER_ASSIGNED",
            "beaconId", beacon.getId(),
            "volunteerId", volunteer.getId(),
            "volunteerName", volunteer.getName(),
            "volunteerLat", volunteer.getCurrentLatitude(),
            "volunteerLng", volunteer.getCurrentLongitude()
        ));
        log.debug("Broadcast VOLUNTEER_ASSIGNED: volunteer #{} → beacon #{}",
                volunteer.getId(), beacon.getId());
    }

    public void broadcastVolunteerLocation(Long volunteerId, double lat, double lng) {
        messagingTemplate.convertAndSend("/topic/volunteers", Map.of(
            "event", "LOCATION_UPDATE",
            "volunteerId", volunteerId,
            "lat", lat,
            "lng", lng
        ));
    }

    public void broadcastZoneUpdate(Object zone) {
        messagingTemplate.convertAndSend("/topic/zones", Map.of(
            "event", "ZONE_UPDATE",
            "data", zone
        ));
    }

    public void broadcastDashboardStats(Map<String, Object> stats) {
        messagingTemplate.convertAndSend("/topic/stats", Map.of(
            "event", "STATS_UPDATE",
            "data", stats
        ));
    }
}
