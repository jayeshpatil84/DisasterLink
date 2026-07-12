package com.disasterlink.repository;

import com.disasterlink.entity.DisasterReport;
import com.disasterlink.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisasterReportRepository extends JpaRepository<DisasterReport, Long> {

    List<DisasterReport> findAllByOrderByCreatedAtDesc();

    long countByStatus(ReportStatus status);
}
