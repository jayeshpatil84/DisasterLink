package com.disasterlink.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Message Broker Service
 *
 * ARCHITECTURE DECISION (important for interviews):
 *
 * Interface designed to be Kafka-compatible:
 *   publish(topic, payload) — identical to KafkaTemplate.send(topic, payload)
 *   subscribe(topic, handler) — mirrors @KafkaListener pattern
 *
 * Prototype uses Redis Pub/Sub:
 *   - Easier local setup (just: redis-server)
 *   - No ZooKeeper, no broker cluster config
 *   - Works identically for low-volume prototype
 *
 * Production migration path:
 *   - Replace RedisTemplate calls with KafkaTemplate
 *   - Keep same topic names: "sos-incoming", "sos-triage-complete", "volunteer-assigned"
 *   - No business logic changes needed
 *
 * Topics:
 *   - sos-incoming: new SOS beacons
 *   - sos-triage-complete: after AI scores
 *   - volunteer-assigned: assignment events
 *   - sms-inbound: SMS parsed events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageBrokerService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(String topic, Map<String, Object> payload) {
        try {
            redisTemplate.convertAndSend(topic, payload);
            log.debug("Published to topic '{}': {}", topic, payload);
        } catch (Exception e) {
            log.error("Failed to publish to topic '{}': {}", topic, e.getMessage());
            // Graceful degradation — don't fail the main flow if broker is down
        }
    }

    // Redis Pub/Sub listener topics (constants match Kafka topic names)
    public static final String TOPIC_SOS_INCOMING = "sos-incoming";
    public static final String TOPIC_SOS_TRIAGE = "sos-triage-complete";
    public static final String TOPIC_VOLUNTEER_ASSIGNED = "volunteer-assigned";
    public static final String TOPIC_SMS_INBOUND = "sms-inbound";
}
