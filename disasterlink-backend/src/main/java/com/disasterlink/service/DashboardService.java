package com.disasterlink.service;

import com.disasterlink.dto.DashboardStatsResponse;
import com.disasterlink.entity.ReportStatus;
import com.disasterlink.repository.DisasterReportRepository;
import org.springframework.stereotype.Service;

/**
 * Computes simple aggregate statistics for the dashboard page.
 */
@Service
public class DashboardService {

    private final DisasterReportRepository reportRepository;

    public DashboardService(DisasterReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public DashboardStatsResponse getStats() {
        long total = reportRepository.count();
        long pending = reportRepository.countByStatus(ReportStatus.PENDING);
        long inProgress = reportRepository.countByStatus(ReportStatus.IN_PROGRESS);
        long resolved = reportRepository.countByStatus(ReportStatus.RESOLVED);

        return new DashboardStatsResponse(total, pending, inProgress, resolved);
    }
}
