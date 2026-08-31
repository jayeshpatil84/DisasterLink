package com.disasterlink.dto;

import com.disasterlink.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for PATCH /api/sos/{id}/status.
 */
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ReportStatus status;

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
}
