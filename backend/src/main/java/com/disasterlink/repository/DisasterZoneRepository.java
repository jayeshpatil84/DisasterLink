package com.disasterlink.repository;

import com.disasterlink.model.DisasterZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisasterZoneRepository extends JpaRepository<DisasterZone, Long> {
    List<DisasterZone> findByActiveTrue();
    List<DisasterZone> findBySeverityAndActiveTrue(DisasterZone.ZoneSeverity severity);
}
