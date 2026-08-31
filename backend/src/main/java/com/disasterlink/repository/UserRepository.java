package com.disasterlink.repository;

import com.disasterlink.entity.Role;
import com.disasterlink.entity.User;
import com.disasterlink.entity.VolunteerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** Used by the officer to populate the volunteer assignment dropdown. */
    List<User> findAllByRole(Role role);

    /** Count volunteers without loading all User entities into memory. */
    long countByRole(Role role);

    /** Returns all volunteers with a specific availability status. */
    List<User> findAllByRoleAndVolunteerStatus(Role role, VolunteerStatus status);

    /** Counts volunteers by availability status. Used for dashboard stats. */
    long countByRoleAndVolunteerStatus(Role role, VolunteerStatus status);

    /**
     * Counts how many active (non-RESOLVED, non-CANCELLED) SOS beacons
     * are assigned to a specific volunteer. Used to compute activeTaskCount.
     */
    @Query("SELECT COUNT(b) FROM SosBeacon b WHERE b.assignedVolunteer.id = :volunteerId " +
           "AND b.status NOT IN (com.disasterlink.entity.ReportStatus.RESOLVED, " +
           "com.disasterlink.entity.ReportStatus.CANCELLED)")
    long countActiveTasksForVolunteer(@Param("volunteerId") Long volunteerId);
}