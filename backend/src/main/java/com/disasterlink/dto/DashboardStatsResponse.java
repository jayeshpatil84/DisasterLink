package com.disasterlink.dto;

/**
 * Extended dashboard statistics shown on the officer dashboard.
 * Includes volunteer availability breakdown and full status breakdown.
 */
public record DashboardStatsResponse(
        long totalBeacons,
        long criticalBeacons,
        long highBeacons,
        long pendingBeacons,
        long assignedBeacons,
        long inProgressBeacons,
        long resolvedBeacons,
        long totalVolunteers,
        long activeVolunteers,
        long availableVolunteers,
        long busyVolunteers
) {}