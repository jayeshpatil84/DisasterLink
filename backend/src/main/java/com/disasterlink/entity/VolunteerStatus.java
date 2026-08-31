package com.disasterlink.entity;

/**
 * Availability state for a VOLUNTEER user.
 *
 * AVAILABLE — volunteer is ready to accept new SOS assignments
 * BUSY      — volunteer is currently handling an active SOS
 * OFFLINE   — volunteer is not reachable / off duty
 *
 * This is stored as a VARCHAR column on the users table via @Enumerated(EnumType.STRING).
 */
public enum VolunteerStatus {
    AVAILABLE,
    BUSY,
    OFFLINE
}
