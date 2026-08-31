package com.disasterlink.dto;

import com.disasterlink.entity.DisasterType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/sos — submitting a new SOS beacon.
 */
public class SosRequest {

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be 10–1000 characters")
    private String description;

    @NotNull(message = "Disaster type is required")
    private DisasterType disasterType;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0",  message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0",  message = "Longitude must be <= 180")
    private Double longitude;

    /** Optional reverse-geocoded address sent from the frontend. */
    private String address;

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
}
