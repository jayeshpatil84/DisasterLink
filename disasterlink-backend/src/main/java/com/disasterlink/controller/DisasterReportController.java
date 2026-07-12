package com.disasterlink.controller;

import com.disasterlink.dto.DisasterReportRequest;
import com.disasterlink.dto.DisasterReportResponse;
import com.disasterlink.dto.StatusUpdateRequest;
import com.disasterlink.service.DisasterReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes CRUD endpoints for disaster reports. All endpoints here
 * require the caller to be authenticated (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/reports")
public class DisasterReportController {

    private final DisasterReportService reportService;

    public DisasterReportController(DisasterReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<DisasterReportResponse>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisasterReportResponse> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @PostMapping
    public ResponseEntity<DisasterReportResponse> createReport(
            @Valid @RequestBody DisasterReportRequest request, Authentication authentication) {
        String username = authentication.getName();
        DisasterReportResponse response = reportService.createReport(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisasterReportResponse> updateReport(
            @PathVariable Long id, @Valid @RequestBody DisasterReportRequest request) {
        return ResponseEntity.ok(reportService.updateReport(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DisasterReportResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(reportService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
