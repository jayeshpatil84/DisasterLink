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
 * REST endpoints exclusively for VOLUNTEER role.
 *
 * GET /api/volunteer/tasks → Returns all SOS records assigned to the currently logged-in volunteer
 */
@RestController
@RequestMapping("/api/volunteer")
@PreAuthorize("hasRole('VOLUNTEER')")
public class VolunteerTaskController {

    private final SosBeaconService beaconService;

    public VolunteerTaskController(SosBeaconService beaconService) {
        this.beaconService = beaconService;
    }

    /**
     * Returns all SOS records assigned to the authenticated volunteer.
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<SosResponse>> getAssignedTasks(Authentication auth) {
        return ResponseEntity.ok(beaconService.getAssignedBeacons(auth.getName()));
    }
}
