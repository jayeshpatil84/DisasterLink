package com.disasterlink.controller;

import com.disasterlink.model.Volunteer;
import com.disasterlink.repository.VolunteerRepository;
import com.disasterlink.service.VolunteerAssignmentService;
import com.disasterlink.websocket.WebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/volunteer")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class VolunteerController {

    private final VolunteerRepository volunteerRepository;
    private final VolunteerAssignmentService assignmentService;
    private final WebSocketBroadcaster webSocketBroadcaster;

    @GetMapping
    @PreAuthorize("hasAnyRole('DISTRICT_OFFICER', 'ADMIN')")
    public ResponseEntity<List<Volunteer>> getAllVolunteers() {
        return ResponseEntity.ok(volunteerRepository.findAll());
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('DISTRICT_OFFICER', 'ADMIN')")
    public ResponseEntity<List<Volunteer>> getAvailableVolunteers() {
        return ResponseEntity.ok(volunteerRepository.findByStatus(Volunteer.VolunteerStatus.AVAILABLE));
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public ResponseEntity<Volunteer> registerVolunteer(@RequestBody Volunteer volunteer) {
        volunteer.setStatus(Volunteer.VolunteerStatus.AVAILABLE);
        volunteer.setRegisteredAt(LocalDateTime.now());
        return ResponseEntity.ok(volunteerRepository.save(volunteer));
    }

    /**
     * Update volunteer GPS location in real-time.
     * Called every 30 seconds from volunteer mobile app.
     */
    @PatchMapping("/{id}/location")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<Void> updateLocation(
            @PathVariable Long id,
            @RequestBody Map<String, Double> coords) {
        volunteerRepository.findById(id).ifPresent(v -> {
            v.setCurrentLatitude(coords.get("lat"));
            v.setCurrentLongitude(coords.get("lng"));
            v.setLocationUpdatedAt(LocalDateTime.now());
            volunteerRepository.save(v);
            webSocketBroadcaster.broadcastVolunteerLocation(id, coords.get("lat"), coords.get("lng"));
        });
        return ResponseEntity.ok().build();
    }

    /**
     * Volunteer marks themselves available again (after completing assignment).
     */
    @PatchMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'DISTRICT_OFFICER', 'ADMIN')")
    public ResponseEntity<Void> releaseVolunteer(@PathVariable Long id) {
        assignmentService.releaseVolunteer(id);
        return ResponseEntity.ok().build();
    }
}
