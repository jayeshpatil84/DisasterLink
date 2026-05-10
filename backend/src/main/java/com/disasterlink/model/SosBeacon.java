package com.disasterlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sos_beacons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SosBeacon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Victim identity (optional - anonymous allowed)
    private String victimName;
    private String contactPhone;

    // Location
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String locationDescription;

    // Situation details
    @Column(nullable = false)
    private String disasterType;     // FLOOD, EARTHQUAKE, FIRE, CYCLONE, OTHER

    @Column(columnDefinition = "TEXT")
    private String situationDetails;

    private Integer affectedCount;   // Number of people affected

    // AI Triage
    @Enumerated(EnumType.STRING)
    private TriagePriority triagePriority;   // CRITICAL, HIGH, MEDIUM, LOW

    private Integer triageScore;             // 0-100 (AI computed)

    @Column(columnDefinition = "TEXT")
    private String triageReason;             // AI explanation

    // Status
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SosStatus status = SosStatus.ACTIVE;

    // Assignment
    private Long assignedVolunteerId;
    private LocalDateTime assignedAt;

    // Metadata
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    // Source channel
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SosSource source = SosSource.APP;   // APP, SMS

    private String rawSmsText;  // for SMS-sourced beacons

    // Enums
    public enum TriagePriority {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    public enum SosStatus {
        ACTIVE, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
    }

    public enum SosSource {
        APP, SMS
    }
}
