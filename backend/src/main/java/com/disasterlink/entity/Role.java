package com.disasterlink.entity;

/**
 * Application roles.
 * - VICTIM:    can submit SOS beacons and view their own history
 * - VOLUNTEER: can view assigned beacons and update their progress
 * - OFFICER:   full access — assign volunteers, change status, view all beacons
 */
public enum Role {
    VICTIM,
    VOLUNTEER,
    OFFICER
}
