package com.disasterlink;

import com.disasterlink.model.SosBeacon;
import com.disasterlink.service.AiTriageService;
import com.disasterlink.service.VolunteerAssignmentService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiTriageServiceTest {

    @Test
    void testHaversineDistance_samePoint_isZero() {
        double dist = VolunteerAssignmentService.haversineKm(
                19.0760, 72.8777, 19.0760, 72.8777);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    void testHaversineDistance_mumbaiToPune_approx148km() {
        // Mumbai: 19.0760, 72.8777
        // Pune:   18.5204, 73.8567
        double dist = VolunteerAssignmentService.haversineKm(
                19.0760, 72.8777, 18.5204, 73.8567);
        // Should be approximately 120-160 km
        assertTrue(dist > 100 && dist < 200,
                "Mumbai-Pune distance should be ~148km, got: " + dist);
    }

    @Test
    void testSosBeacon_defaultStatus_isActive() {
        SosBeacon beacon = SosBeacon.builder()
                .latitude(19.0760)
                .longitude(72.8777)
                .disasterType("FLOOD")
                .build();

        assertEquals(SosBeacon.SosStatus.ACTIVE, beacon.getStatus());
        assertEquals(SosBeacon.SosSource.APP, beacon.getSource());
    }

    @Test
    void testSosBeacon_triagePriority_defaultMedium() {
        SosBeacon beacon = SosBeacon.builder()
                .latitude(18.5)
                .longitude(73.8)
                .disasterType("FIRE")
                .triagePriority(SosBeacon.TriagePriority.MEDIUM)
                .triageScore(50)
                .build();

        assertEquals(SosBeacon.TriagePriority.MEDIUM, beacon.getTriagePriority());
        assertEquals(50, beacon.getTriageScore());
    }
}
