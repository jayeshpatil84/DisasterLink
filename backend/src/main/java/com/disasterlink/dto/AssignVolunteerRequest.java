package com.disasterlink.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for PATCH /api/sos/{id}/assign.
 * An officer sends this to assign a volunteer to an open SOS beacon.
 */
public class AssignVolunteerRequest {

    @NotNull(message = "Volunteer ID is required")
    private Long volunteerId;

    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }
}
