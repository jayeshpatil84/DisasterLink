package com.disasterlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "disaster_zones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisasterZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String disasterType;

    // Bounding circle (center + radius in km)
    private Double centerLatitude;
    private Double centerLongitude;
    private Double radiusKm;

    // Severity: RED (critical), ORANGE (high), YELLOW (medium), GREEN (safe)
    @Enumerated(EnumType.STRING)
    private ZoneSeverity severity;

    private Integer estimatedAffected;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    private boolean active;

    public enum ZoneSeverity {
        RED, ORANGE, YELLOW, GREEN
    }
}
