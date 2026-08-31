package com.disasterlink.service;

import com.disasterlink.dto.SosResponse;
import com.disasterlink.dto.WebSocketNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Broadcasts SOS events over WebSocket (STOMP protocol).
 *
 * Topics used:
 *   /topic/sos/new           → new SOS submitted (all officers)
 *   /topic/sos/resolved      → SOS resolved (all officers)
 *   /topic/sos-feed          → all SOS events (kept for backward-compat with existing dashboard)
 *   /topic/volunteer/{id}/task      → new/updated task for a specific volunteer
 *   /topic/victim/{id}/sos-update   → SOS update for a specific victim
 */
@Service
public class WebSocketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Pushes a new/updated SOS beacon to all connected clients on the general feed.
     * Kept for backward compatibility with the existing dashboard component.
     */
    public void broadcast(SosResponse beacon, String event) {
        try {
            messagingTemplate.convertAndSend("/topic/sos-feed", beacon);
            log.debug("WebSocket broadcast [{}] beacon #{}", event, beacon.getId());
        } catch (Exception e) {
            log.warn("WebSocket broadcast failed for beacon #{}: {}", beacon.getId(), e.getMessage());
        }
    }

    /**
     * Notifies all officers that a new SOS has arrived.
     * Officers subscribe to /topic/sos/new on their dashboard.
     */
    public void notifyNewSos(WebSocketNotification notification) {
        sendToTopic("/topic/sos/new", notification, "NEW_SOS");
    }

    /**
     * Notifies all officers that an SOS has been resolved.
     * Officers subscribe to /topic/sos/resolved on their dashboard.
     */
    public void notifySosResolved(WebSocketNotification notification) {
        sendToTopic("/topic/sos/resolved", notification, "SOS_RESOLVED");
    }

    /**
     * Sends a task notification to a specific volunteer.
     * Volunteers subscribe to /topic/volunteer/{volunteerId}/task.
     */
    public void notifyVolunteer(Long volunteerId, WebSocketNotification notification) {
        sendToTopic("/topic/volunteer/" + volunteerId + "/task", notification, "VOLUNTEER_TASK");
    }

    /**
     * Sends a status update to the victim whose SOS was updated.
     * Victims subscribe to /topic/victim/{victimId}/sos-update.
     */
    public void notifyVictim(Long victimId, WebSocketNotification notification) {
        sendToTopic("/topic/victim/" + victimId + "/sos-update", notification, "VICTIM_UPDATE");
    }

    private void sendToTopic(String topic, Object payload, String eventLabel) {
        try {
            messagingTemplate.convertAndSend(topic, payload);
            log.debug("WebSocket [{}] → {}", eventLabel, topic);
        } catch (Exception e) {
            log.warn("WebSocket send failed [{}] to {}: {}", eventLabel, topic, e.getMessage());
        }
    }
}
