package com.disasterlink.service;

import com.disasterlink.dto.SosResponse;
import com.disasterlink.dto.VolunteerInfoResponse;
import com.disasterlink.entity.ReportStatus;
import com.disasterlink.entity.Role;
import com.disasterlink.entity.SosBeacon;
import com.disasterlink.entity.UrgencyLabel;
import com.disasterlink.entity.User;
import com.disasterlink.repository.SosBeaconRepository;
import com.disasterlink.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for officer-specific operations.
 * Officers can:
 *  - View all volunteers with their current status and active task count
 *  - Filter SOS beacons by urgency level and/or status
 */
@Service
public class OfficerService {

    private final SosBeaconRepository beaconRepository;
    private final UserRepository userRepository;

    public OfficerService(SosBeaconRepository beaconRepository, UserRepository userRepository) {
        this.beaconRepository = beaconRepository;
        this.userRepository   = userRepository;
    }

    /**
     * Returns all volunteers with their availability status and active task count.
     */
    @Transactional(readOnly = true)
    public List<VolunteerInfoResponse> getAllVolunteers() {
        List<User> volunteers = userRepository.findAllByRole(Role.VOLUNTEER);
        return volunteers.stream()
                .map(v -> new VolunteerInfoResponse(
                        v.getId(),
                        v.getUsername(),
                        v.getVolunteerStatus(),
                        userRepository.countActiveTasksForVolunteer(v.getId())))
                .toList();
    }

    /**
     * Returns filtered SOS beacons.
     * Both parameters are optional — null means "no filter on that dimension".
     */
    @Transactional(readOnly = true)
    public List<SosResponse> getAllSos(String urgencyLevelStr, String statusStr) {
        UrgencyLabel urgencyLabel = null;
        ReportStatus status = null;

        if (urgencyLevelStr != null && !urgencyLevelStr.isBlank()) {
            try {
                urgencyLabel = UrgencyLabel.valueOf(urgencyLevelStr.toUpperCase());
            } catch (IllegalArgumentException ignored) { /* invalid filter → no filter */ }
        }
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = ReportStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) { /* invalid filter → no filter */ }
        }

        List<SosBeacon> beacons;
        if (urgencyLabel != null && status != null) {
            beacons = beaconRepository.findAllByUrgencyLabelAndStatusOrderByCreatedAtDesc(urgencyLabel, status);
        } else if (urgencyLabel != null) {
            beacons = beaconRepository.findAllByUrgencyLabelOrderByCreatedAtDesc(urgencyLabel);
        } else if (status != null) {
            beacons = beaconRepository.findAllByStatusOrderByCreatedAtDesc(status);
        } else {
            beacons = beaconRepository.findAllByOrderByUrgencyScoreDescCreatedAtDesc();
        }

        return beacons.stream()
                .map(SosResponse::fromEntity)
                .toList();
    }
}
