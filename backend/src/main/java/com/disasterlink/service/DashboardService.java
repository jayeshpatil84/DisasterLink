package com.disasterlink.service;

import com.disasterlink.dto.DashboardStatsResponse;
import com.disasterlink.entity.ReportStatus;
import com.disasterlink.entity.Role;
import com.disasterlink.entity.UrgencyLabel;
import com.disasterlink.entity.VolunteerStatus;
import com.disasterlink.repository.SosBeaconRepository;
import com.disasterlink.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregates statistics for the officer dashboard cards.
 */
@Service
public class DashboardService {

    private final SosBeaconRepository beaconRepository;
    private final UserRepository userRepository;

    public DashboardService(SosBeaconRepository beaconRepository, UserRepository userRepository) {
        this.beaconRepository = beaconRepository;
        this.userRepository   = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long total       = beaconRepository.count();
        long critical    = beaconRepository.countByUrgencyLabel(UrgencyLabel.CRITICAL);
        long high        = beaconRepository.countByUrgencyLabel(UrgencyLabel.HIGH);
        long pending     = beaconRepository.countByStatus(ReportStatus.PENDING);
        long assigned    = beaconRepository.countByStatus(ReportStatus.ASSIGNED);
        long inProgress  = beaconRepository.countByStatus(ReportStatus.IN_PROGRESS);
        long resolved    = beaconRepository.countByStatus(ReportStatus.RESOLVED);
        long totalVols   = userRepository.countByRole(Role.VOLUNTEER);
        long activeVols  = beaconRepository.countActiveVolunteers();
        long availVols   = userRepository.countByRoleAndVolunteerStatus(Role.VOLUNTEER, VolunteerStatus.AVAILABLE);
        long busyVols    = userRepository.countByRoleAndVolunteerStatus(Role.VOLUNTEER, VolunteerStatus.BUSY);

        return new DashboardStatsResponse(
                total, critical, high, pending, assigned,
                inProgress, resolved, totalVols, activeVols, availVols, busyVols
        );
    }
}