package com.disasterlink.entity;

/**
 * Lifecycle states for an SOS beacon.
 *
 * Full transition order enforced by SosBeaconService:
 *   PENDING → ASSIGNED → EN_ROUTE → ARRIVED → IN_PROGRESS → RESOLVED
 *
 * CANCELLED  — false alarm or victim withdrew the SOS (only from PENDING)
 */
public enum ReportStatus {
    PENDING,
    ASSIGNED,
    EN_ROUTE,
    ARRIVED,
    IN_PROGRESS,
    RESOLVED,
    CANCELLED
}
