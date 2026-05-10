package com.disasterlink.dto;

import com.disasterlink.model.SosBeacon;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SosBeaconResponse {

    private Long id;
    private String victimName;
    private String contactPhone;
    private Double latitude;
    private Double longitude;
    private String locationDescription;
    private String disasterType;
    private String situationDetails;
    private Integer affectedCount;
    private String triagePriority;
    private Integer triageScore;
    private String triageReason;
    private String status;
    private String source;
    private Long assignedVolunteerId;
    private LocalDateTime assignedAt;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static SosBeaconResponse from(SosBeacon beacon) {
        return SosBeaconResponse.builder()
                .id(beacon.getId())
                .victimName(beacon.getVictimName())
                .contactPhone(beacon.getContactPhone())
                .latitude(beacon.getLatitude())
                .longitude(beacon.getLongitude())
                .locationDescription(beacon.getLocationDescription())
                .disasterType(beacon.getDisasterType())
                .situationDetails(beacon.getSituationDetails())
                .affectedCount(beacon.getAffectedCount())
                .triagePriority(beacon.getTriagePriority() != null
                        ? beacon.getTriagePriority().name() : "MEDIUM")
                .triageScore(beacon.getTriageScore())
                .triageReason(beacon.getTriageReason())
                .status(beacon.getStatus().name())
                .source(beacon.getSource().name())
                .assignedVolunteerId(beacon.getAssignedVolunteerId())
                .assignedAt(beacon.getAssignedAt())
                .createdAt(beacon.getCreatedAt())
                .resolvedAt(beacon.getResolvedAt())
                .build();
    }
}
