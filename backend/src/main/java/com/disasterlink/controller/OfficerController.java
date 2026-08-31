package com.disasterlink.controller;

import com.disasterlink.dto.AssignVolunteerRequest;
import com.disasterlink.dto.DashboardStatsResponse;
import com.disasterlink.dto.SosResponse;
import com.disasterlink.dto.VolunteerInfoResponse;
import com.disasterlink.service.DashboardService;
import com.disasterlink.service.OfficerService;
import com.disasterlink.service.SosBeaconService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints exclusively for OFFICER role.
 *
 * GET  /api/officer/stats        → extended dashboard statistics
 * GET  /api/officer/volunteers   → all volunteers with status and task count
 * GET  /api/officer/sos          → all SOS with optional filters
 * PATCH /api/sos/{id}/reassign   → reassign a different volunteer to an SOS
 */
@RestController
@PreAuthorize("hasRole('OFFICER')")
public class OfficerController {

    private final DashboardService dashboardService;
    private final OfficerService officerService;
    private final SosBeaconService beaconService;

    public OfficerController(DashboardService dashboardService,
                              OfficerService officerService,
                              SosBeaconService beaconService) {
        this.dashboardService = dashboardService;
        this.officerService   = officerService;
        this.beaconService    = beaconService;
    }

    /**
     * Returns extended statistics: total, critical, high, pending, assigned,
     * in-progress, resolved, total volunteers, active volunteers, available, busy.
     */
    @GetMapping("/api/officer/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /**
     * Returns all volunteers with name, volunteerStatus, and number of active tasks.
     */
    @GetMapping("/api/officer/volunteers")
    public ResponseEntity<List<VolunteerInfoResponse>> getVolunteers() {
        return ResponseEntity.ok(officerService.getAllVolunteers());
    }

    /**
     * Returns all SOS records with optional query parameter filters.
     * @param urgencyLevel optional filter: CRITICAL | HIGH | MEDIUM | LOW
     * @param status       optional filter: PENDING | ASSIGNED | EN_ROUTE | ARRIVED | IN_PROGRESS | RESOLVED
     */
    @GetMapping("/api/officer/sos")
    public ResponseEntity<List<SosResponse>> getAllSos(
            @RequestParam(required = false) String urgencyLevel,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(officerService.getAllSos(urgencyLevel, status));
    }

    /**
     * Reassigns an SOS to a different AVAILABLE volunteer.
     * Old volunteer is freed (set back to AVAILABLE), new volunteer becomes BUSY.
     */
    @PatchMapping("/api/sos/{id}/reassign")
    public ResponseEntity<SosResponse> reassignVolunteer(
            @PathVariable Long id,
            @Valid @RequestBody AssignVolunteerRequest request,
            Authentication auth) {
        return ResponseEntity.ok(beaconService.reassignVolunteer(id, request, auth.getName()));
    }
}
