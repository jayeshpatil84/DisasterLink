package com.disasterlink.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * The core domain object — a geo-tagged SOS beacon submitted by a victim.
 *
 * Lifecycle:
 *  1. Victim submits → status = PENDING, Gemini triage runs automatically
 *  2. Officer sees it on the map → assigns a volunteer
 *  3. Volunteer responds → status = IN_PROGRESS
 *  4. Situation handled  → status = RESOLVED
 *
 * FIX H7: Timestamps are now set in @PrePersist / @PreUpdate lifecycle callbacks
 * rather than as field initializers. Field initializers run at object construction,
 * so when Hibernate re-hydrates an entity from the database, the initializer briefly
 * overwrites the DB value before Hibernate sets the actual column value via setter.
 * @PrePersist only fires on INSERT, guaranteeing correct semantics.
 */
@Entity
@Table(
    name = "sos_beacons",
    indexes = {
        @Index(name = "idx_sos_status", columnList = "status"),
        @Index(name = "idx_sos_urgency_label", columnList = "urgency_label"),
        @Index(name = "idx_sos_reporter", columnList = "reporter_id"),
        @Index(name = "idx_sos_volunteer", columnList = "assigned_volunteer_id"),
        @Index(name = "idx_sos_created_at", columnList = "created_at")
    }
)
public class SosBeacon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Description of the emergency written by the victim. */
    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisasterType disasterType;

    /** GPS latitude of the incident location. */
    @Column(nullable = false)
    private Double latitude;

    /** GPS longitude of the incident location. */
    @Column(nullable = false)
    private Double longitude;

    /** Optional human-readable address (reverse-geocoded by the frontend). */
    @Column(length = 250)
    private String address;

    /**
     * Urgency score computed by Gemini AI, or the rule-based fallback.
     * Range: 0 (no urgency) – 100 (extreme/life-threatening).
     */
    @Column(nullable = false)
    private int urgencyScore = 0;

    /** Human-readable urgency tier derived from the score. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UrgencyLabel urgencyLabel = UrgencyLabel.LOW;

    /**
     * Short note explaining how the urgency was determined.
     * Example: "AI triage: fire with reported casualties → CRITICAL"
     * or       "Rule-based fallback: EARTHQUAKE type → HIGH"
     */
    @Column(length = 500)
    private String triageNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    /** Victim who submitted this SOS. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /**
     * Volunteer assigned to handle this beacon.
     * Null until an officer makes an assignment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_volunteer_id")
    private User assignedVolunteer;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public SosBeacon() {}

    /**
     * FIX H7: Set timestamps here, not in field initializers.
     * @PrePersist runs immediately before INSERT — the entity is fully initialized
     * and all fields have been set by the calling service.
     */
    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public User getAssignedVolunteer() { return assignedVolunteer; }
    public void setAssignedVolunteer(User assignedVolunteer) { this.assignedVolunteer = assignedVolunteer; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}