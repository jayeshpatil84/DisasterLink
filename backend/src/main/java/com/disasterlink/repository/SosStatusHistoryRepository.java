package com.disasterlink.repository;

import com.disasterlink.entity.SosStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Provides read/write access to the sos_status_history table.
 */
@Repository
public interface SosStatusHistoryRepository extends JpaRepository<SosStatusHistory, Long> {

    /** Returns all history entries for a given SOS, ordered chronologically. */
    List<SosStatusHistory> findAllBySosIdOrderByChangedAtAsc(Long sosId);
}
