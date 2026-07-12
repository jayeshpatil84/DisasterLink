package com.disasterlink.dto;

import com.disasterlink.entity.DisasterReport;
import com.disasterlink.entity.DisasterType;
import com.disasterlink.entity.ReportStatus;
import java.time.LocalDateTime;

/**
 * Response shape returned to the frontend for a disaster report.
 * Keeps the User relationship flattened to just the reporter's username.
 */
public class DisasterReportResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private DisasterType disasterType;
    private ReportStatus status;
    private String reportedByUsername;
    private LocalDateTime createdAt;

    public DisasterReportResponse() {
    }

    public static DisasterReportResponse fromEntity(DisasterReport report) {
        DisasterReportResponse response = new DisasterReportResponse();
        response.setId(report.getId());
        response.setTitle(report.getTitle());
        response.setDescription(report.getDescription());
        response.setLocation(report.getLocation());
        response.setDisasterType(report.getDisasterType());
        response.setStatus(report.getStatus());
        response.setReportedByUsername(report.getReportedBy().getUsername());
        response.setCreatedAt(report.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DisasterType getDisasterType() {
        return disasterType;
    }

    public void setDisasterType(DisasterType disasterType) {
        this.disasterType = disasterType;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getReportedByUsername() {
        return reportedByUsername;
    }

    public void setReportedByUsername(String reportedByUsername) {
        this.reportedByUsername = reportedByUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
