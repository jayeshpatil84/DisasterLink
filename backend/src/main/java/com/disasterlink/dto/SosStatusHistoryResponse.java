package com.disasterlink.dto;

import java.time.LocalDateTime;

/**
 * Represents one entry in the SOS status change timeline.
 * Returned by GET /api/sos/{id}/history.
 */
public record SosStatusHistoryResponse(
        Long id,
        Long sosId,
        String oldStatus,
        String newStatus,
        String changedBy,
        LocalDateTime changedAt
) {}
