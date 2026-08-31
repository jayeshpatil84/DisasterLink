package com.disasterlink;

import com.disasterlink.dto.AssignVolunteerRequest;
import com.disasterlink.dto.RegisterRequest;
import com.disasterlink.dto.SosRequest;
import com.disasterlink.dto.SosResponse;
import com.disasterlink.dto.StatusUpdateRequest;
import com.disasterlink.entity.DisasterType;
import com.disasterlink.entity.ReportStatus;
import com.disasterlink.entity.Role;
import com.disasterlink.entity.User;
import com.disasterlink.entity.VolunteerStatus;
import com.disasterlink.exception.InvalidStatusTransitionException;
import com.disasterlink.exception.UnauthorizedException;
import com.disasterlink.repository.SosBeaconRepository;
import com.disasterlink.repository.UserRepository;
import com.disasterlink.service.AuthService;
import com.disasterlink.service.SosBeaconService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SosWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SosBeaconService beaconService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SosBeaconRepository beaconRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private User victim;
    private User volunteer;
    private User officer;

    @BeforeEach
    void setUp() {
        beaconRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create victim
        RegisterRequest victimReq = new RegisterRequest();
        victimReq.setUsername("victim1");
        victimReq.setEmail("victim1@test.com");
        victimReq.setPassword("pass123");
        victimReq.setRole(Role.VICTIM);
        authService.register(victimReq);
        victim = userRepository.findByUsername("victim1").orElseThrow();

        // 2. Create volunteer
        RegisterRequest volReq = new RegisterRequest();
        volReq.setUsername("vol1");
        volReq.setEmail("vol1@test.com");
        volReq.setPassword("pass123");
        volReq.setRole(Role.VOLUNTEER);
        authService.register(volReq);
        volunteer = userRepository.findByUsername("vol1").orElseThrow();

        // 3. Create officer
        officer = new User("officer1", "officer1@test.com", passwordEncoder.encode("pass123"), Role.OFFICER);
        officer = userRepository.save(officer);
    }

    @Test
    void test1_victimCanCreateSos_andUrgencyIsCalculated() {
        SosRequest req = new SosRequest();
        req.setDescription("Severe flood water entering ground floor, trapped families");
        req.setDisasterType(DisasterType.FLOOD);
        req.setLatitude(19.0760);
        req.setLongitude(72.8777);
        req.setAddress("Mumbai, Maharashtra");

        SosResponse res = beaconService.createBeacon(req, "victim1");

        assertNotNull(res.getId());
        assertEquals(ReportStatus.PENDING, res.getStatus());
        assertTrue(res.getUrgencyScore() > 0, "Urgency score should be computed");
        assertNotNull(res.getUrgencyLabel());
    }

    @Test
    void test2_officerCanAssignAvailableVolunteer() {
        // Create SOS
        SosRequest req = new SosRequest();
        req.setDescription("Fire on 3rd floor");
        req.setDisasterType(DisasterType.FIRE);
        req.setLatitude(19.0760);
        req.setLongitude(72.8777);
        SosResponse created = beaconService.createBeacon(req, "victim1");

        // Verify volunteer is AVAILABLE initially
        User vol = userRepository.findById(volunteer.getId()).orElseThrow();
        assertEquals(VolunteerStatus.AVAILABLE, vol.getVolunteerStatus());

        // Assign volunteer
        AssignVolunteerRequest assignReq = new AssignVolunteerRequest();
        assignReq.setVolunteerId(volunteer.getId());
        SosResponse assigned = beaconService.assignVolunteer(created.getId(), assignReq, "officer1");

        assertEquals(ReportStatus.ASSIGNED, assigned.getStatus());
        assertEquals(volunteer.getId(), assigned.getVolunteerId());

        // Verify volunteer status is now BUSY
        User updatedVol = userRepository.findById(volunteer.getId()).orElseThrow();
        assertEquals(VolunteerStatus.BUSY, updatedVol.getVolunteerStatus());
    }

    @Test
    void test3_assigningBusyVolunteer_throwsException() {
        // Set volunteer to BUSY
        volunteer.setVolunteerStatus(VolunteerStatus.BUSY);
        userRepository.save(volunteer);

        // Create SOS
        SosRequest req = new SosRequest();
        req.setDescription("Landslide on highway");
        req.setDisasterType(DisasterType.LANDSLIDE);
        req.setLatitude(19.0760);
        req.setLongitude(72.8777);
        SosResponse created = beaconService.createBeacon(req, "victim1");

        AssignVolunteerRequest assignReq = new AssignVolunteerRequest();
        assignReq.setVolunteerId(volunteer.getId());

        assertThrows(UnauthorizedException.class, () ->
                beaconService.assignVolunteer(created.getId(), assignReq, "officer1"));
    }

    @Test
    void test4_statusTransitionOrder() {
        // Create and assign
        SosRequest req = new SosRequest();
        req.setDescription("Medical emergency cardiac event");
        req.setDisasterType(DisasterType.MEDICAL);
        req.setLatitude(19.0760);
        req.setLongitude(72.8777);
        SosResponse created = beaconService.createBeacon(req, "victim1");

        // PENDING -> RESOLVED directly should fail
        StatusUpdateRequest invalidReq = new StatusUpdateRequest();
        invalidReq.setStatus(ReportStatus.RESOLVED);
        assertThrows(InvalidStatusTransitionException.class, () ->
                beaconService.updateStatus(created.getId(), invalidReq, "officer1", Role.OFFICER));

        // Assign volunteer: PENDING -> ASSIGNED
        AssignVolunteerRequest assignReq = new AssignVolunteerRequest();
        assignReq.setVolunteerId(volunteer.getId());
        beaconService.assignVolunteer(created.getId(), assignReq, "officer1");

        // Step 1: ASSIGNED -> EN_ROUTE (valid)
        StatusUpdateRequest enRouteReq = new StatusUpdateRequest();
        enRouteReq.setStatus(ReportStatus.EN_ROUTE);
        SosResponse step1 = beaconService.updateStatus(created.getId(), enRouteReq, "vol1", Role.VOLUNTEER);
        assertEquals(ReportStatus.EN_ROUTE, step1.getStatus());

        // Step 2: EN_ROUTE -> ARRIVED (valid)
        StatusUpdateRequest arrivedReq = new StatusUpdateRequest();
        arrivedReq.setStatus(ReportStatus.ARRIVED);
        SosResponse step2 = beaconService.updateStatus(created.getId(), arrivedReq, "vol1", Role.VOLUNTEER);
        assertEquals(ReportStatus.ARRIVED, step2.getStatus());

        // Step 3: ARRIVED -> IN_PROGRESS (valid)
        StatusUpdateRequest inProgReq = new StatusUpdateRequest();
        inProgReq.setStatus(ReportStatus.IN_PROGRESS);
        SosResponse step3 = beaconService.updateStatus(created.getId(), inProgReq, "vol1", Role.VOLUNTEER);
        assertEquals(ReportStatus.IN_PROGRESS, step3.getStatus());

        // Step 4: IN_PROGRESS -> RESOLVED (valid)
        StatusUpdateRequest resolvedReq = new StatusUpdateRequest();
        resolvedReq.setStatus(ReportStatus.RESOLVED);
        SosResponse step4 = beaconService.updateStatus(created.getId(), resolvedReq, "vol1", Role.VOLUNTEER);
        assertEquals(ReportStatus.RESOLVED, step4.getStatus());

        // Volunteer should be reset to AVAILABLE after RESOLVED
        User volAfterResolve = userRepository.findById(volunteer.getId()).orElseThrow();
        assertEquals(VolunteerStatus.AVAILABLE, volAfterResolve.getVolunteerStatus());
    }

    @Test
    void test5_volunteerCanOnlySeeOwnTasks() {
        // Create 2 tasks, assign only 1 to vol1
        SosRequest req1 = new SosRequest();
        req1.setDescription("Accident on bridge");
        req1.setDisasterType(DisasterType.ACCIDENT);
        req1.setLatitude(19.0760);
        req1.setLongitude(72.8777);
        SosResponse sos1 = beaconService.createBeacon(req1, "victim1");

        AssignVolunteerRequest assignReq = new AssignVolunteerRequest();
        assignReq.setVolunteerId(volunteer.getId());
        beaconService.assignVolunteer(sos1.getId(), assignReq, "officer1");

        List<SosResponse> tasks = beaconService.getAssignedBeacons("vol1");
        assertEquals(1, tasks.size());
        assertEquals(sos1.getId(), tasks.get(0).getId());
    }

    @Test
    @WithMockUser(username = "victim1", roles = {"VICTIM"})
    void test6_victimAccessingOfficerStats_returns403() throws Exception {
        mockMvc.perform(get("/api/officer/stats"))
                .andExpect(status().isForbidden());
    }
}
