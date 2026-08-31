package com.disasterlink.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * An application user. Roles determine what they can see and do:
 *   VICTIM    — submits SOS beacons
 *   VOLUNTEER — gets assigned to beacons by an officer
 *   OFFICER   — manages the full incident lifecycle and volunteer assignment
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_volunteer_status", columnList = "volunteer_status")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** BCrypt-hashed password; the plain-text value is never stored. */
    @Column(nullable = false)
    private String password;

    /** The role drives all authorization decisions throughout the app. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Role role = Role.VICTIM;

    /**
     * Availability state for volunteers only.
     * Set to AVAILABLE when a volunteer registers.
     * Set to BUSY when assigned to an active SOS.
     * Set back to AVAILABLE when SOS is RESOLVED.
     * Null for VICTIM and OFFICER users (not meaningful for those roles).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "volunteer_status", length = 15)
    private VolunteerStatus volunteerStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public User() {}

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        // Auto-set AVAILABLE for volunteers on first save
        if (this.role == Role.VOLUNTEER && this.volunteerStatus == null) {
            this.volunteerStatus = VolunteerStatus.AVAILABLE;
        }
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public VolunteerStatus getVolunteerStatus() { return volunteerStatus; }
    public void setVolunteerStatus(VolunteerStatus volunteerStatus) { this.volunteerStatus = volunteerStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
