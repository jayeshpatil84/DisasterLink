package com.disasterlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SosBeaconRequest {

    private String victimName;
    private String contactPhone;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String locationDescription;

    @NotBlank(message = "Disaster type is required")
    private String disasterType;

    private String situationDetails;
    private Integer affectedCount;
}
