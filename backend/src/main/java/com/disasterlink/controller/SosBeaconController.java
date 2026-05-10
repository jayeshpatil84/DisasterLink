package com.disasterlink.controller;

import com.disasterlink.dto.SosBeaconRequest;
import com.disasterlink.dto.SosBeaconResponse;
import com.disasterlink.model.SosBeacon;
import com.disasterlink.service.SosBeaconService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sos")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class SosBeaconController {

    private final SosBeaconService sosBeaconService;

    /**
     * POST /api/sos
     * Create a new SOS beacon. Available to all authenticated users (victims).
     * Returns immediately — AI triage runs async.
     */
    @PostMapping
    public ResponseEntity<SosBeaconResponse> createSos(
            @Valid @RequestBody SosBeaconRequest request) {
        SosBeaconResponse response = sosBeaconService.createSosBeacon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/sos/sms-webhook
     * Twilio inbound SMS webhook. No auth (Twilio signs requests with X-Twilio-Signature).
     * In production: validate Twilio signature.
     */
    @PostMapping("/sms-webhook")
    public ResponseEntity<String> receiveSms(
            @RequestParam("From") String from,
            @RequestParam("Body") String body,
            @RequestParam(value = "Latitude", required = false) Double lat,
            @RequestParam(value = "Longitude", required = false) Double lng) {

        sosBeaconService.createSosFromSms(from, body, lat, lng);

        // Twilio expects TwiML response
        return ResponseEntity.ok("""
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Message>SOS received. DisasterLink is coordinating help. Stay safe.</Message>
            </Response>
            """);
    }

    /**
     * GET /api/sos/active
     * All active beacons — for district officer map view.
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'DISTRICT_OFFICER', 'ADMIN')")
    public ResponseEntity<List<SosBeaconResponse>> getActiveBeacons() {
        return ResponseEntity.ok(sosBeaconService.getActiveBeacons());
    }

    /**
     * GET /api/sos
     * All beacons (including resolved) — for admin view.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DISTRICT_OFFICER', 'ADMIN')")
    public ResponseEntity<List<SosBeaconResponse>> getAllBeacons() {
        return ResponseEntity.ok(sosBeaconService.getAllBeacons());
    }

    /**
     * PATCH /api/sos/{id}/status
     * Update beacon status (DISTRICT_OFFICER or ADMIN only).
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DISTRICT_OFFICER', 'ADMIN')")
    public ResponseEntity<SosBeaconResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        SosBeacon.SosStatus status = SosBeacon.SosStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(sosBeaconService.updateStatus(id, status));
    }
}
