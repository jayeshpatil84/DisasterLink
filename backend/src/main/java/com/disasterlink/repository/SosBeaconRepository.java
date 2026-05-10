package com.disasterlink.repository;

import com.disasterlink.model.SosBeacon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SosBeaconRepository extends JpaRepository<SosBeacon, Long> {

    List<SosBeacon> findByStatusOrderByTriageScoreDesc(SosBeacon.SosStatus status);

    List<SosBeacon> findByStatusInOrderByTriagePriorityAscCreatedAtAsc(
            List<SosBeacon.SosStatus> statuses);

    List<SosBeacon> findByAssignedVolunteerId(Long volunteerId);

    // Find beacons within radius (Haversine approximation using bounding box)
    @Query("""
        SELECT s FROM SosBeacon s
        WHERE s.status = 'ACTIVE'
        AND s.latitude BETWEEN :minLat AND :maxLat
        AND s.longitude BETWEEN :minLng AND :maxLng
        ORDER BY s.triageScore DESC
        """)
    List<SosBeacon> findActiveInBoundingBox(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng);

    @Query("SELECT COUNT(s) FROM SosBeacon s WHERE s.status = :status")
    long countByStatus(@Param("status") SosBeacon.SosStatus status);

    @Query("SELECT COUNT(s) FROM SosBeacon s WHERE s.triagePriority = 'CRITICAL' AND s.status = 'ACTIVE'")
    long countActiveCritical();
}
