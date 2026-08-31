package com.disasterlink.repository;

import com.disasterlink.entity.ReportStatus;
import com.disasterlink.entity.SosBeacon;
import com.disasterlink.entity.UrgencyLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SosBeaconRepository extends JpaRepository<SosBeacon, Long> {

    /** All beacons ordered by most-urgent first, then most-recent. */
    List<SosBeacon> findAllByOrderByUrgencyScoreDescCreatedAtDesc();

    /** A specific victim's own beacons. */
    List<SosBeacon> findAllByReporterUsernameOrderByCreatedAtDesc(String username);

    /** Beacons assigned to a specific volunteer. */
    List<SosBeacon> findAllByAssignedVolunteerUsernameOrderByCreatedAtDesc(String username);

    long countByStatus(ReportStatus status);

    long countByUrgencyLabel(UrgencyLabel label);

    /** Filter by urgency level only. */
    List<SosBeacon> findAllByUrgencyLabelOrderByCreatedAtDesc(UrgencyLabel urgencyLabel);

    /** Filter by status only. */
    List<SosBeacon> findAllByStatusOrderByCreatedAtDesc(ReportStatus status);

    /** Filter by both urgency level and status. */
    List<SosBeacon> findAllByUrgencyLabelAndStatusOrderByCreatedAtDesc(UrgencyLabel urgencyLabel, ReportStatus status);

    /**
     * Number of volunteers who have at least one beacon currently active (not RESOLVED or CANCELLED).
     * Used for the "active volunteers" dashboard stat.
     */
    @Query("SELECT COUNT(DISTINCT b.assignedVolunteer.id) FROM SosBeacon b " +
           "WHERE b.status NOT IN (com.disasterlink.entity.ReportStatus.RESOLVED, " +
           "com.disasterlink.entity.ReportStatus.CANCELLED) " +
           "AND b.assignedVolunteer IS NOT NULL")
    long countActiveVolunteers();
}