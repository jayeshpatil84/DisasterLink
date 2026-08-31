package com.disasterlink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.disasterlink.entity.DisasterType;
import com.disasterlink.entity.UrgencyLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Determines urgency for an SOS beacon in two ways:
 *
 *  1. PRIMARY — Google Gemini Flash API
 *     Sends the disaster type + description to Gemini and asks it to return
 *     a JSON object with a score (0–100) and a label (CRITICAL/HIGH/MEDIUM/LOW).
 *
 *  2. FALLBACK — Rule-based scoring
 *     Used when the Gemini API key is missing, the API is unreachable,
 *     or the response cannot be parsed. Guarantees 100% uptime.
 *
 * FIX M3:
 * - HttpClient is now a static final shared instance. HttpClient is thread-safe
 *   and meant to be shared; creating a new instance per service bean wastes
 *   thread pool and connection pool resources.
 * - ObjectMapper is injected via constructor (Spring auto-configures one with
 *   correct settings for the Jackson version in use). Creating ObjectMapper with
 *   `new ObjectMapper()` is expensive and misses Spring Boot's auto-configuration
 *   (e.g., JavaTimeModule for LocalDateTime serialization).
 */
@Service
public class GeminiTriageService {

    private static final Logger log = LoggerFactory.getLogger(GeminiTriageService.class);

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    // FIX M3: Shared, thread-safe HttpClient as a static constant
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    // FIX M3: Injected ObjectMapper (Spring Boot auto-configures this correctly)
    private final ObjectMapper objectMapper;

    public GeminiTriageService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Scores the SOS beacon and returns a TriageResult.
     * Never throws — falls back to rule-based on any failure.
     */
    public TriageResult triage(String description, DisasterType disasterType) {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                return callGemini(description, disasterType);
            } catch (Exception e) {
                log.warn("Gemini triage failed ({}); falling back to rule-based.", e.getMessage());
            }
        }
        return ruleBased(description, disasterType);
    }

    // -------------------------------------------------------------------------
    // Gemini API call
    // -------------------------------------------------------------------------

    private TriageResult callGemini(String description, DisasterType disasterType) throws Exception {
        String prompt = buildPrompt(description, disasterType);

        // Build the JSON body Gemini expects
        String requestBody = """
                {
                  "contents": [{
                    "parts": [{"text": %s}]
                  }],
                  "generationConfig": {
                    "responseMimeType": "application/json"
                  }
                }
                """.formatted(objectMapper.writeValueAsString(prompt));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL + geminiApiKey))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini returned HTTP " + response.statusCode());
        }

        // Parse Gemini's response envelope → extract the text part → parse our JSON
        JsonNode root        = objectMapper.readTree(response.body());
        String   textContent = root.at("/candidates/0/content/parts/0/text").asText();
        JsonNode result      = objectMapper.readTree(textContent);

        int    score = result.get("score").asInt();
        String label = result.get("label").asText();
        String note  = result.has("reasoning") ? result.get("reasoning").asText() : "AI triage";

        return new TriageResult(score, UrgencyLabel.valueOf(label), "AI: " + note);
    }

    private String buildPrompt(String description, DisasterType disasterType) {
        return """
                You are a disaster triage system. Analyze this emergency SOS report and return ONLY a JSON object.

                Disaster type: %s
                Description: %s

                Return exactly this JSON structure (no markdown, no explanation):
                {
                  "score": <integer 0-100>,
                  "label": <"CRITICAL"|"HIGH"|"MEDIUM"|"LOW">,
                  "reasoning": <one-sentence explanation>
                }

                Scoring guide:
                - 80-100 → CRITICAL: immediate life threat, mass casualties, structural collapse
                - 60-79  → HIGH: serious injuries, fire spreading, large flood
                - 40-59  → MEDIUM: property damage, minor injuries, manageable situation
                - 0-39   → LOW: precautionary, no injuries reported, stable situation
                """.formatted(disasterType, description);
    }

    // -------------------------------------------------------------------------
    // Rule-based fallback
    // -------------------------------------------------------------------------

    private TriageResult ruleBased(String description, DisasterType disasterType) {
        int score = baseScoreForType(disasterType);
        score += urgencyKeywordBoost(description);
        // Clamp to 0–100
        score = Math.max(0, Math.min(100, score));

        UrgencyLabel label = labelFromScore(score);
        String note = "Rule-based fallback: %s type → score %d".formatted(disasterType, score);
        log.info(note);
        return new TriageResult(score, label, note);
    }

    private int baseScoreForType(DisasterType type) {
        return switch (type) {
            case EARTHQUAKE -> 80;
            case CYCLONE, TSUNAMI -> 78;
            case FIRE -> 70;
            case LANDSLIDE, FLOOD -> 65;
            case MEDICAL -> 60;
            case ACCIDENT -> 55;
            case OTHER -> 40;
        };
    }

    /** Boosts score when the description contains high-urgency keywords. */
    private int urgencyKeywordBoost(String description) {
        if (description == null) return 0;
        String lower = description.toLowerCase();
        int boost = 0;
        if (lower.contains("dead") || lower.contains("death") || lower.contains("killed")) boost += 18;
        if (lower.contains("trapped") || lower.contains("buried") || lower.contains("collapse")) boost += 15;
        if (lower.contains("unconscious") || lower.contains("critical") || lower.contains("bleeding")) boost += 12;
        if (lower.contains("children") || lower.contains("baby") || lower.contains("elderly")) boost += 8;
        if (lower.contains("help") || lower.contains("urgent") || lower.contains("emergency")) boost += 5;
        return boost;
    }

    private UrgencyLabel labelFromScore(int score) {
        if (score >= 80) return UrgencyLabel.CRITICAL;
        if (score >= 60) return UrgencyLabel.HIGH;
        if (score >= 40) return UrgencyLabel.MEDIUM;
        return UrgencyLabel.LOW;
    }

    // -------------------------------------------------------------------------
    // Result record
    // -------------------------------------------------------------------------

    /** Simple value object carrying the triage output. */
    public record TriageResult(int score, UrgencyLabel label, String note) {}
}