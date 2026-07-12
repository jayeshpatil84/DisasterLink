package com.disasterlink.dto;

/**
 * Simple aggregate statistics shown on the dashboard.
 */
public class DashboardStatsResponse {

    private long totalReports;
    private long pendingReports;
    private long inProgressReports;
    private long resolvedReports;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(long totalReports, long pendingReports,
                                   long inProgressReports, long resolvedReports) {
        this.totalReports = totalReports;
        this.pendingReports = pendingReports;
        this.inProgressReports = inProgressReports;
        this.resolvedReports = resolvedReports;
    }

    public long getTotalReports() {
        return totalReports;
    }

    public void setTotalReports(long totalReports) {
        this.totalReports = totalReports;
    }

    public long getPendingReports() {
        return pendingReports;
    }

    public void setPendingReports(long pendingReports) {
        this.pendingReports = pendingReports;
    }

    public long getInProgressReports() {
        return inProgressReports;
    }

    public void setInProgressReports(long inProgressReports) {
        this.inProgressReports = inProgressReports;
    }

    public long getResolvedReports() {
        return resolvedReports;
    }

    public void setResolvedReports(long resolvedReports) {
        this.resolvedReports = resolvedReports;
    }
}
