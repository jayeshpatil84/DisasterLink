package com.disasterlink.service;

import com.disasterlink.entity.UrgencyLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends push notifications to ntfy.sh for high-urgency SOS beacons.
 *
 * ntfy.sh is a free, open-source push notification service.
 * Users (officers/volunteers) subscribe to the topic via:
 *   - ntfy mobile app (Android/iOS) — subscribe to your configured topic
 *   - Browser at https://ntfy.sh/<topic>
 *   - Curl: curl -s https://ntfy.sh/<topic>/json
 *
 * Notifications are sent asynchronously so they never block the main request.
 * Failures are logged but do NOT affect the SOS submission.
 */
@Service
public class NtfyNotificationService {

    private static final Logger log = LoggerFactory.getLogger(NtfyNotificationService.class);

    /** ntfy topic name — configure in application.properties. */
    @Value("${ntfy.topic:disasterlink-sos-alerts}")
    private String ntfyTopic;

    @Value("${ntfy.base-url:https://ntfy.sh}")
    private String ntfyBaseUrl;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Sends a push notification for CRITICAL or HIGH urgency beacons.
     * Runs in a background thread (non-blocking).
     *
     * @param beaconId     the SOS beacon ID
     * @param disasterType e.g. "FIRE"
     * @param urgencyLabel the urgency tier
     * @param address      human-readable location (may be null)
     */
    @Async
    public void notifyIfUrgent(Long beaconId, String disasterType,
                                UrgencyLabel urgencyLabel, String address) {
        if (urgencyLabel != UrgencyLabel.CRITICAL && urgencyLabel != UrgencyLabel.HIGH) {
            return; // only push for serious events
        }

        String emoji   = urgencyLabel == UrgencyLabel.CRITICAL ? "🚨" : "⚠️";
        String title   = emoji + " " + urgencyLabel + " SOS — " + disasterType;
        String message = address != null && !address.isBlank()
                ? "Incident at: " + address + " (ID #" + beaconId + ")"
                : "SOS beacon #" + beaconId + " requires immediate attention";
        String priority = urgencyLabel == UrgencyLabel.CRITICAL ? "urgent" : "high";
        String tags     = urgencyLabel == UrgencyLabel.CRITICAL ? "rotating_light,sos" : "warning";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ntfyBaseUrl + "/" + ntfyTopic))
                    .header("Title", title)
                    .header("Priority", priority)
                    .header("Tags", tags)
                    .timeout(Duration.ofSeconds(8))
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("ntfy notification sent for beacon #{}: HTTP {}", beaconId, response.statusCode());

        } catch (Exception e) {
            // Notification failure must never affect SOS submission
            log.warn("ntfy notification failed for beacon #{}: {}", beaconId, e.getMessage());
        }
    }
}
