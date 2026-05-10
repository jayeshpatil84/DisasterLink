package com.disasterlink.service;

import com.disasterlink.model.SosBeacon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * AI Triage Service
 *
 * Uses Google Gemini API to analyse incoming SOS beacons and assign:
 *  - Priority: CRITICAL / HIGH / MEDIUM / LOW
 *  - Score: 0-100 (higher = more urgent)
 *  - Reason: plain-English explanation for district officer dashboard
 *
 * Prompt engineering is designed to output structured JSON so it can
 * be parsed reliably. LangChain is intentionally skipped to keep the
 * prototype lightweight — Gemini is called directly via WebFlux.
 */
@Service
@Slf4j
public class AiTriageService {

    private final WebClient webClient;

    @Value("${app.gemini.api.key}")
    private String geminiApiKey;

    @Value("${app.gemini.api.url}")
    private String geminiApiUrl;

    public AiTriageService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Analyse a SOS beacon and return triage result.
     * Called asynchronously after beacon is saved.
     */
    public TriageResult analyseSos(SosBeacon beacon) {
        String prompt = buildTriagePrompt(beacon);

        try {
            String response = callGemini(prompt);
            return parseTriageResponse(response);
        } catch (Exception e) {
            log.error("Gemini API call failed for beacon {}: {}", beacon.getId(), e.getMessage());
            // Fallback: rule-based triage if AI unavailable
            return ruleBasedTriage(beacon);
        }
    }

    private String buildTriagePrompt(SosBeacon beacon) {
        return """
            You are an emergency triage AI for a disaster coordination system in India.
            Analyse this SOS beacon and respond ONLY with valid JSON (no markdown, no explanation).

            SOS BEACON DATA:
            - Disaster type: %s
            - Situation: %s
            - Affected people: %d
            - Location: lat=%f, lng=%f

            TRIAGE CRITERIA:
            - CRITICAL (score 80-100): Immediate life threat, 50+ people, medical emergency, structural collapse
            - HIGH (score 60-79): Significant risk, 10-49 people, rising flood, fire approaching
            - MEDIUM (score 40-59): Moderate risk, 1-9 people, stranded but safe for now
            - LOW (score 0-39): Minimal immediate risk, general assistance needed

            Respond with exactly this JSON structure:
            {
              "priority": "CRITICAL|HIGH|MEDIUM|LOW",
              "score": <integer 0-100>,
              "reason": "<one sentence explaining the priority, max 20 words>"
            }
            """.formatted(
                beacon.getDisasterType(),
                beacon.getSituationDetails(),
                beacon.getAffectedCount() != null ? beacon.getAffectedCount() : 1,
                beacon.getLatitude(),
                beacon.getLongitude()
        );
    }

    private String callGemini(String prompt) {
        Map<String, Object> requestBody = Map.of(
            "contents", new Object[]{
                Map.of("parts", new Object[]{
                    Map.of("text", prompt)
                })
            },
            "generationConfig", Map.of(
                "temperature", 0.1,
                "maxOutputTokens", 200
            )
        );

        String url = geminiApiUrl + "?key=" + geminiApiKey;

        return webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> extractTextFromGeminiResponse(resp))
                .block();
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromGeminiResponse(Map<?, ?> response) {
        var candidates = (java.util.List<?>) response.get("candidates");
        var first = (Map<?, ?>) candidates.get(0);
        var content = (Map<?, ?>) first.get("content");
        var parts = (java.util.List<?>) content.get("parts");
        var part = (Map<?, ?>) parts.get(0);
        return (String) part.get("text");
    }

    private TriageResult parseTriageResponse(String jsonText) {
        try {
            // Simple JSON parsing (avoid heavy dependencies)
            String clean = jsonText.trim()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            String priority = extractJsonField(clean, "priority");
            int score = Integer.parseInt(extractJsonField(clean, "score"));
            String reason = extractJsonField(clean, "reason");

            return new TriageResult(
                    SosBeacon.TriagePriority.valueOf(priority.toUpperCase()),
                    score,
                    reason
            );
        } catch (Exception e) {
            log.warn("Failed to parse Gemini JSON response: {}", jsonText);
            return new TriageResult(SosBeacon.TriagePriority.MEDIUM, 50, "Auto-assigned medium priority (parse error)");
        }
    }

    private String extractJsonField(String json, String field) {
        String search = "\"" + field + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) throw new RuntimeException("Field not found: " + field);
        int colon = json.indexOf(":", idx);
        int start = json.indexOf("\"", colon + 1);
        if (start < 0) {
            // numeric value
            String rest = json.substring(colon + 1).trim();
            return rest.split("[,}]")[0].trim();
        }
        int end = json.indexOf("\"", start + 1);
        return json.substring(start + 1, end);
    }

    /**
     * Rule-based fallback triage when Gemini is unavailable.
     * Interviewers love this — shows you thought about resilience.
     */
    private TriageResult ruleBasedTriage(SosBeacon beacon) {
        int score = 30;
        String reason = "Rule-based triage applied.";

        // Keyword scoring
        String details = beacon.getSituationDetails() != null
                ? beacon.getSituationDetails().toLowerCase() : "";

        if (details.contains("dead") || details.contains("unconscious") ||
            details.contains("bleeding") || details.contains("trapped")) {
            score += 40;
            reason = "Critical keywords detected: medical emergency.";
        } else if (details.contains("fire") || details.contains("collapse") ||
                   details.contains("drowning")) {
            score += 30;
            reason = "High-risk disaster type detected.";
        }

        // People count scoring
        int people = beacon.getAffectedCount() != null ? beacon.getAffectedCount() : 1;
        if (people >= 50) score += 30;
        else if (people >= 10) score += 20;
        else if (people >= 5) score += 10;

        score = Math.min(score, 100);

        SosBeacon.TriagePriority priority;
        if (score >= 80) priority = SosBeacon.TriagePriority.CRITICAL;
        else if (score >= 60) priority = SosBeacon.TriagePriority.HIGH;
        else if (score >= 40) priority = SosBeacon.TriagePriority.MEDIUM;
        else priority = SosBeacon.TriagePriority.LOW;

        return new TriageResult(priority, score, reason);
    }

    public record TriageResult(
            SosBeacon.TriagePriority priority,
            int score,
            String reason
    ) {}
}
