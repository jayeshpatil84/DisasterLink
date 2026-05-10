package com.disasterlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "volunteers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    // Current GPS location (updated in real-time via WebSocket)
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime locationUpdatedAt;

    // Skills (stored as comma-separated string for simplicity)
    // Examples: MEDICAL, RESCUE, BOAT_OPERATOR, STRUCTURAL_ENGINEER, LOGISTICS
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_skills", joinColumns = @JoinColumn(name = "volunteer_id"))
    @Column(name = "skill")
    private Set<String> skills;

    // Status
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VolunteerStatus status = VolunteerStatus.AVAILABLE;

    // Current assignment
    private Long currentSosId;
    private LocalDateTime assignedAt;

    // Metadata
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    private Integer totalAssignments;

    // Linked User account
    private Long userId;

    public enum VolunteerStatus {
        AVAILABLE, ASSIGNED, BUSY, OFFLINE
    }
}
