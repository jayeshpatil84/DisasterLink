package com.disasterlink.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Records every status change on an SOS beacon.
 * This gives officers and victims a full audit trail / timeline of how a beacon progressed.
 *
 * Example entries:
 *   PENDING     → ASSIGNED     (changedBy = "officer1")
 *   ASSIGNED    → EN_ROUTE     (changedBy = "volunteer1")
 *   EN_ROUTE    → ARRIVED      (changedBy = "volunteer1")
 *   ARRIVED     → IN_PROGRESS  (changedBy = "volunteer1")
 *   IN_PROGRESS → RESOLVED     (changedBy = "volunteer1")
 */
@Entity
@Table(name = "sos_status_history", indexes = {
        @Index(name = "idx_history_sos_id", columnList = "sos_id"),
        @Index(name = "idx_history_changed_at", columnList = "changed_at")
})
public class SosStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The SOS beacon this history entry belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_id", nullable = false)
    private SosBeacon sos;

    /** The status before the change. Null for the first entry (beacon creation). */
    @Column(name = "old_status", length = 20)
    private String oldStatus;

    /** The status after the change. */
    @Column(name = "new_status", nullable = false, length = 20)
    private String newStatus;

    /** Username of the user who made the change (officer, volunteer, or system). */
    @Column(name = "changed_by", nullable = false, length = 50)
    private String changedBy;

    /** Timestamp when the change occurred. */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public SosStatusHistory() {}

    /** Convenience constructor. */
    public SosStatusHistory(SosBeacon sos, String oldStatus, String newStatus, String changedBy) {
        this.sos = sos;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SosBeacon getSos() { return sos; }
    public void setSos(SosBeacon sos) { this.sos = sos; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
