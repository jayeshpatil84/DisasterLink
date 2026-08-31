package com.disasterlink.controller;

import com.disasterlink.dto.SosResponse;
import com.disasterlink.service.SosBeaconService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints exclusively for VICTIM role.
 *
 * GET /api/victim/my-sos → Returns all SOS records submitted by the currently logged-in victim
 */
@RestController
@RequestMapping("/api/victim")
@PreAuthorize("hasRole('VICTIM')")
public class VictimController {

    private final SosBeaconService beaconService;

    public VictimController(SosBeaconService beaconService) {
        this.beaconService = beaconService;
    }

    /**
     * Returns all SOS records submitted by the authenticated victim.
     * Includes current status, assigned volunteer name, urgency level, etc.
     */
    @GetMapping("/my-sos")
    public ResponseEntity<List<SosResponse>> getMySos(Authentication auth) {
        return ResponseEntity.ok(beaconService.getMyBeacons(auth.getName()));
    }
}
