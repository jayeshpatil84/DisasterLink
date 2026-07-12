package com.disasterlink.service;

import com.disasterlink.dto.DisasterReportRequest;
import com.disasterlink.dto.DisasterReportResponse;
import com.disasterlink.entity.DisasterReport;
import com.disasterlink.entity.ReportStatus;
import com.disasterlink.entity.User;
import com.disasterlink.exception.ResourceNotFoundException;
import com.disasterlink.repository.DisasterReportRepository;
import com.disasterlink.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Contains the business logic for creating, reading, updating,
 * and deleting disaster reports.
 */
@Service
public class DisasterReportService {

    private final DisasterReportRepository reportRepository;
    private final UserRepository userRepository;

    public DisasterReportService(DisasterReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    public List<DisasterReportResponse> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(DisasterReportResponse::fromEntity)
                .toList();
    }

    public DisasterReportResponse getReportById(Long id) {
        DisasterReport report = findReportOrThrow(id);
        return DisasterReportResponse.fromEntity(report);
    }

    public DisasterReportResponse createReport(DisasterReportRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        DisasterReport report = new DisasterReport();
        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setLocation(request.getLocation());
        report.setDisasterType(request.getDisasterType());
        report.setStatus(ReportStatus.PENDING);
        report.setReportedBy(user);

        DisasterReport saved = reportRepository.save(report);
        return DisasterReportResponse.fromEntity(saved);
    }

    public DisasterReportResponse updateReport(Long id, DisasterReportRequest request) {
        DisasterReport report = findReportOrThrow(id);

        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setLocation(request.getLocation());
        report.setDisasterType(request.getDisasterType());

        DisasterReport updated = reportRepository.save(report);
        return DisasterReportResponse.fromEntity(updated);
    }

    public DisasterReportResponse updateStatus(Long id, ReportStatus status) {
        DisasterReport report = findReportOrThrow(id);
        report.setStatus(status);
        DisasterReport updated = reportRepository.save(report);
        return DisasterReportResponse.fromEntity(updated);
    }

    public void deleteReport(Long id) {
        DisasterReport report = findReportOrThrow(id);
        reportRepository.delete(report);
    }

    private DisasterReport findReportOrThrow(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster report not found with id: " + id));
    }
}
