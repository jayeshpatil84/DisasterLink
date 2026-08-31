package com.disasterlink.dto;

import com.disasterlink.entity.DisasterType;
import com.disasterlink.entity.ReportStatus;
import com.disasterlink.entity.SosBeacon;
import com.disasterlink.entity.UrgencyLabel;

import java.time.LocalDateTime;

/**
 * Read-only view of an SOS beacon returned to the frontend.
 * Flattens the entity's lazy-loaded relationships into flat fields
 * so we never accidentally trigger extra SQL queries during serialization.
 */
public class SosResponse {

    private Long id;
    private String description;
    private DisasterType disasterType;
    private Double latitude;
    private Double longitude;
    private String address;
    private int urgencyScore;
    private UrgencyLabel urgencyLabel;
    private String triageNote;
    private ReportStatus status;

    // Reporter info (flattened from User)
    private Long reporterId;
    private String reporterUsername;

    // Assigned volunteer info (null if not yet assigned)
    private Long volunteerId;
    private String volunteerUsername;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Constructs a response DTO from the JPA entity. */
    public static SosResponse fromEntity(SosBeacon beacon) {
        SosResponse dto = new SosResponse();
        dto.id            = beacon.getId();
        dto.description   = beacon.getDescription();
        dto.disasterType  = beacon.getDisasterType();
        dto.latitude      = beacon.getLatitude();
        dto.longitude     = beacon.getLongitude();
        dto.address       = beacon.getAddress();
        dto.urgencyScore  = beacon.getUrgencyScore();
        dto.urgencyLabel  = beacon.getUrgencyLabel();
        dto.triageNote    = beacon.getTriageNote();
        dto.status        = beacon.getStatus();
        dto.createdAt     = beacon.getCreatedAt();
        dto.updatedAt     = beacon.getUpdatedAt();

        if (beacon.getReporter() != null) {
            dto.reporterId       = beacon.getReporter().getId();
            dto.reporterUsername = beacon.getReporter().getUsername();
        }
        if (beacon.getAssignedVolunteer() != null) {
            dto.volunteerId       = beacon.getAssignedVolunteer().getId();
            dto.volunteerUsername = beacon.getAssignedVolunteer().getUsername();
        }
        return dto;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DisasterType getDisasterType() { return disasterType; }
    public void setDisasterType(DisasterType disasterType) { this.disasterType = disasterType; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getUrgencyScore() { return urgencyScore; }
    public void setUrgencyScore(int urgencyScore) { this.urgencyScore = urgencyScore; }

    public UrgencyLabel getUrgencyLabel() { return urgencyLabel; }
    public void setUrgencyLabel(UrgencyLabel urgencyLabel) { this.urgencyLabel = urgencyLabel; }

    public String getTriageNote() { return triageNote; }
    public void setTriageNote(String triageNote) { this.triageNote = triageNote; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }

    public String getVolunteerUsername() { return volunteerUsername; }
    public void setVolunteerUsername(String volunteerUsername) { this.volunteerUsername = volunteerUsername; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
