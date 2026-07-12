package com.disasterlink.dto;

import com.disasterlink.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Payload used to update only the status of a disaster report.
 */
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ReportStatus status;

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }
}
