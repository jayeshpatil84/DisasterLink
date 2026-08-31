package com.disasterlink.entity;

/**
 * Urgency classification for an SOS beacon after AI triage.
 *
 * Score thresholds:
 *   CRITICAL  ≥ 80  — immediate threat to life
 *   HIGH      ≥ 60  — serious injury or large-scale event
 *   MEDIUM    ≥ 40  — manageable situation
 *   LOW       < 40  — minor or precautionary
 */
public enum UrgencyLabel {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}
