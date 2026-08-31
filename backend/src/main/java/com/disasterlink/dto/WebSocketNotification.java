package com.disasterlink.dto;

/**
 * Simple WebSocket push notification payload.
 * Sent to targeted topics when SOS events occur.
 *
 * Example:
 * {
 *   "sosId": 101,
 *   "eventType": "VOLUNTEER_ASSIGNED",
 *   "message": "A volunteer has been assigned to your SOS"
 * }
 */
public class WebSocketNotification {

    private Long sosId;
    private String eventType;
    private String message;

    public WebSocketNotification() {}

    public WebSocketNotification(Long sosId, String eventType, String message) {
        this.sosId = sosId;
        this.eventType = eventType;
        this.message = message;
    }

    public Long getSosId() { return sosId; }
    public void setSosId(Long sosId) { this.sosId = sosId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
