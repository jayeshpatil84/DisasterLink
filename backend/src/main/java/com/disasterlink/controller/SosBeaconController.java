package com.disasterlink.controller;

import com.disasterlink.dto.AssignVolunteerRequest;
import com.disasterlink.dto.SosRequest;
import com.disasterlink.dto.SosResponse;
import com.disasterlink.dto.SosStatusHistoryResponse;
import com.disasterlink.dto.StatusUpdateRequest;
import com.disasterlink.entity.Role;
import com.disasterlink.service.SosBeaconService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for SOS beacon operations.
 *
 * POST   /api/sos               → submit a new SOS beacon (victim/any authenticated)
 * GET    /api/sos               → list all beacons (officer/volunteer)
 * GET    /api/sos/my            → list own beacons (victim)
 * GET    /api/sos/assigned      → list assigned beacons (volunteer)
 * GET    /api/sos/{id}          → get single beacon
 * GET    /api/sos/{id}/history  → get timeline / status history for a beacon
 * PATCH  /api/sos/{id}/assign   → assign volunteer (officer only)
 * PATCH  /api/sos/{id}/status   → update status (volunteer for own task, officer)
 * DELETE /api/sos/{id}          → cancel/delete beacon
 */
@RestController
@RequestMapping("/api/sos")
public class SosBeaconController {

    private final SosBeaconService beaconService;

    public SosBeaconController(SosBeaconService beaconService) {
        this.beaconService = beaconService;
    }

    @PostMapping
    public ResponseEntity<SosResponse> submitSos(
            @Valid @RequestBody SosRequest request,
            Authentication auth) {
        SosResponse response = beaconService.createBeacon(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SosResponse>> getAllBeacons() {
        return ResponseEntity.ok(beaconService.getAllBeacons());
    }

    @GetMapping("/my")
    public ResponseEntity<List<SosResponse>> getMyBeacons(Authentication auth) {
        return ResponseEntity.ok(beaconService.getMyBeacons(auth.getName()));
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<SosResponse>> getAssignedBeacons(Authentication auth) {
        return ResponseEntity.ok(beaconService.getAssignedBeacons(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SosResponse> getBeaconById(@PathVariable Long id, Authentication auth) {
        Role role = extractRole(auth);
        return ResponseEntity.ok(beaconService.getBeaconById(id, auth.getName(), role));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<SosStatusHistoryResponse>> getBeaconHistory(@PathVariable Long id) {
        return ResponseEntity.ok(beaconService.getHistory(id));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<SosResponse> assignVolunteer(
            @PathVariable Long id,
            @Valid @RequestBody AssignVolunteerRequest request,
            Authentication auth) {
        return ResponseEntity.ok(beaconService.assignVolunteer(id, request, auth.getName()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SosResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            Authentication auth) {
        Role role = extractRole(auth);
        return ResponseEntity.ok(beaconService.updateStatus(id, request, auth.getName(), role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBeacon(
            @PathVariable Long id,
            Authentication auth) {
        Role role = extractRole(auth);
        beaconService.deleteBeacon(id, auth.getName(), role);
        return ResponseEntity.ok(Map.of("message", "SOS beacon cancelled successfully"));
    }

    /**
     * Derives the application Role enum from the Spring Security GrantedAuthority.
     * The authority string is "ROLE_VICTIM", "ROLE_VOLUNTEER", or "ROLE_OFFICER".
     */
    private Role extractRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> Role.valueOf(a.getAuthority().replace("ROLE_", "")))
                .orElseThrow(() -> new IllegalStateException("No role authority found in token"));
    }
}