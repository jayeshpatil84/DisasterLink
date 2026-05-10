package com.disasterlink.repository;

import com.disasterlink.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    List<Volunteer> findByStatus(Volunteer.VolunteerStatus status);

    Optional<Volunteer> findByUserId(Long userId);

    Optional<Volunteer> findByPhone(String phone);

    // Find available volunteers with a specific skill
    @Query("""
        SELECT v FROM Volunteer v
        JOIN v.skills s
        WHERE v.status = 'AVAILABLE'
        AND s = :skill
        AND v.currentLatitude IS NOT NULL
        """)
    List<Volunteer> findAvailableBySkill(@Param("skill") String skill);

    // Find nearest available volunteers using bounding box
    @Query("""
        SELECT v FROM Volunteer v
        WHERE v.status = 'AVAILABLE'
        AND v.currentLatitude BETWEEN :minLat AND :maxLat
        AND v.currentLongitude BETWEEN :minLng AND :maxLng
        """)
    List<Volunteer> findAvailableInBoundingBox(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng);

    @Query("SELECT COUNT(v) FROM Volunteer v WHERE v.status = 'AVAILABLE'")
    long countAvailable();
}
