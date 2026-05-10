package com.disasterlink.service;

import com.disasterlink.model.SosBeacon;
import com.disasterlink.model.Volunteer;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SMS Service (Twilio)
 *
 * Handles:
 * 1. Outbound: notify volunteer of assignment
 * 2. Outbound: confirm SOS receipt to victim
 * 3. Inbound: parsed by TwilioWebhookController
 *
 * SMS is critical for India context — network bandwidth collapses
 * during disasters. SMS works on 2G when apps can't.
 */
@Service
@Slf4j
public class SmsService {

    @Value("${app.twilio.account.sid}")
    private String accountSid;

    @Value("${app.twilio.auth.token}")
    private String authToken;

    @Value("${app.twilio.phone.number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        if (!accountSid.startsWith("YOUR")) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS service initialized");
        } else {
            log.warn("Twilio credentials not configured - SMS will be logged only");
        }
    }

    /**
     * Send assignment notification to volunteer.
     * Works on any phone, no app needed.
     */
    public void notifyVolunteerAssignment(Volunteer volunteer, SosBeacon beacon) {
        if (volunteer.getPhone() == null) return;

        String message = buildVolunteerSms(volunteer, beacon);
        sendSms(volunteer.getPhone(), message);
    }

    /**
     * Confirm SOS receipt to victim.
     */
    public void confirmSosReceipt(SosBeacon beacon, Volunteer assignedVolunteer) {
        if (beacon.getContactPhone() == null) return;

        String message;
        if (assignedVolunteer != null) {
            message = String.format(
                "[DisasterLink] SOS #%d received. Volunteer %s has been assigned and is on the way. Stay safe.",
                beacon.getId(), assignedVolunteer.getName()
            );
        } else {
            message = String.format(
                "[DisasterLink] SOS #%d received and logged. Priority: %s. Help is being coordinated.",
                beacon.getId(), beacon.getTriagePriority()
            );
        }

        sendSms(beacon.getContactPhone(), message);
    }

    /**
     * Alert district officer for CRITICAL events.
     */
    public void alertDistrictOfficer(String officerPhone, SosBeacon beacon) {
        String message = String.format(
            "[DisasterLink] CRITICAL ALERT: %s near lat=%f,lng=%f. %d affected. Beacon #%d. Immediate response required.",
            beacon.getDisasterType(),
            beacon.getLatitude(),
            beacon.getLongitude(),
            beacon.getAffectedCount() != null ? beacon.getAffectedCount() : 0,
            beacon.getId()
        );
        sendSms(officerPhone, message);
    }

    private String buildVolunteerSms(Volunteer volunteer, SosBeacon beacon) {
        return String.format(
            "[DisasterLink] ASSIGNMENT: Hi %s, you have been assigned to SOS #%d. " +
            "Type: %s | Location: https://maps.google.com/?q=%f,%f | " +
            "People: %d | Priority: %s. Reply CONFIRM or DECLINE.",
            volunteer.getName(),
            beacon.getId(),
            beacon.getDisasterType(),
            beacon.getLatitude(),
            beacon.getLongitude(),
            beacon.getAffectedCount() != null ? beacon.getAffectedCount() : 1,
            beacon.getTriagePriority()
        );
    }

    private void sendSms(String toPhone, String body) {
        if (accountSid.startsWith("YOUR")) {
            // Dev mode: log instead of sending
            log.info("[SMS SIMULATION] To: {} | Body: {}", toPhone, body);
            return;
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(fromNumber),
                    body
            ).create();
            log.info("SMS sent to {} | SID: {}", toPhone, message.getSid());
        } catch (Exception e) {
            log.error("SMS send failed to {}: {}", toPhone, e.getMessage());
        }
    }
}
