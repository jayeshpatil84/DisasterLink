package com.disasterlink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;

/**
 * Configures a STOMP-over-WebSocket message broker.
 *
 * Clients connect to: ws://localhost:8080/ws  (or SockJS fallback)
 * Subscribe to live feed: /topic/sos-feed
 * Send messages to server: /app/... (not used for now — backend pushes only)
 *
 * FIX M6: WebSocket allowed origins are now driven by the same cors.allowed-origins
 * property as the REST API, preventing wildcard WebSocket origin in production.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOriginsRaw;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic → in-memory pub/sub topics (broadcast to all subscribers)
        registry.enableSimpleBroker("/topic");
        // /app  → prefix for messages routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // FIX M6: Use explicit allowed origins from env config instead of wildcard
        String[] origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins)
                // SockJS provides a polling fallback for browsers that block WebSockets
                .withSockJS();
    }
}