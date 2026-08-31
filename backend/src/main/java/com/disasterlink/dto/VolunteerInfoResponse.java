package com.disasterlink.dto;

import com.disasterlink.entity.VolunteerStatus;

/**
 * Response DTO representing a volunteer's availability and workload.
 * Returned by GET /api/officer/volunteers.
 */
public class VolunteerInfoResponse {

    private Long id;
    private String username;
    private VolunteerStatus volunteerStatus;
    private long activeTaskCount;

    public VolunteerInfoResponse() {}

    public VolunteerInfoResponse(Long id, String username, VolunteerStatus volunteerStatus, long activeTaskCount) {
        this.id = id;
        this.username = username;
        this.volunteerStatus = volunteerStatus;
        this.activeTaskCount = activeTaskCount;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public VolunteerStatus getVolunteerStatus() { return volunteerStatus; }
    public void setVolunteerStatus(VolunteerStatus volunteerStatus) { this.volunteerStatus = volunteerStatus; }

    public long getActiveTaskCount() { return activeTaskCount; }
    public void setActiveTaskCount(long activeTaskCount) { this.activeTaskCount = activeTaskCount; }
}
