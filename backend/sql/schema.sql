-- DisasterLink v2.0 — Database Schema (MySQL)
-- NOTE: Hibernate auto-creates / updates tables via spring.jpa.hibernate.ddl-auto=update
-- This file is for reference and manual setup only.

CREATE DATABASE IF NOT EXISTS disasterlink_db;
USE disasterlink_db;

-- ========================
-- Table: users
-- ========================
CREATE TABLE IF NOT EXISTS users (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    email             VARCHAR(100) NOT NULL UNIQUE,
    password          VARCHAR(255) NOT NULL, 
    role              VARCHAR(15)  NOT NULL DEFAULT 'VICTIM',  -- VICTIM | VOLUNTEER | OFFICER
    volunteer_status  VARCHAR(15)  DEFAULT 'AVAILABLE',        -- AVAILABLE | BUSY | OFFLINE (for VOLUNTEER role)
    created_at        DATETIME     NOT NULL
);

-- Seed an officer account (password: officer123 BCrypt-hashed)
-- Run this after first startup to create your admin/officer account:
-- INSERT INTO users (username, email, password, role, volunteer_status, created_at)
-- VALUES ('officer1', 'officer@disasterlink.com',
--         '$2a$10$examplehashchangethisbeforeuse', 'OFFICER', NULL, NOW());

-- ========================
-- Table: sos_beacons
-- ========================
CREATE TABLE IF NOT EXISTS sos_beacons (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    description             VARCHAR(1000) NOT NULL,
    disaster_type           VARCHAR(20)   NOT NULL,  -- FLOOD|FIRE|EARTHQUAKE|CYCLONE|LANDSLIDE|TSUNAMI|ACCIDENT|MEDICAL|OTHER
    latitude                DOUBLE        NOT NULL,
    longitude               DOUBLE        NOT NULL,
    address                 VARCHAR(250),
    urgency_score           INT           NOT NULL DEFAULT 0,     -- 0–100
    urgency_label           VARCHAR(10)   NOT NULL DEFAULT 'LOW', -- CRITICAL|HIGH|MEDIUM|LOW
    triage_note             VARCHAR(500),
    status                  VARCHAR(20)   NOT NULL DEFAULT 'PENDING', -- PENDING|ASSIGNED|EN_ROUTE|ARRIVED|IN_PROGRESS|RESOLVED|CANCELLED
    reporter_id             BIGINT        NOT NULL,
    assigned_volunteer_id   BIGINT,
    created_at              DATETIME      NOT NULL,
    updated_at              DATETIME      NOT NULL,
    CONSTRAINT fk_beacon_reporter   FOREIGN KEY (reporter_id)           REFERENCES users(id),
    CONSTRAINT fk_beacon_volunteer  FOREIGN KEY (assigned_volunteer_id) REFERENCES users(id)
);

-- ========================
-- Table: sos_status_history
-- ========================
CREATE TABLE IF NOT EXISTS sos_status_history (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    sos_id      BIGINT       NOT NULL,
    old_status  VARCHAR(20),
    new_status  VARCHAR(20)  NOT NULL,
    changed_by  VARCHAR(50)  NOT NULL,
    changed_at  DATETIME     NOT NULL,
    CONSTRAINT fk_history_sos FOREIGN KEY (sos_id) REFERENCES sos_beacons(id) ON DELETE CASCADE
);

CREATE INDEX idx_sos_status          ON sos_beacons (status);
CREATE INDEX idx_sos_urgency_label   ON sos_beacons (urgency_label);
CREATE INDEX idx_sos_reporter        ON sos_beacons (reporter_id);
CREATE INDEX idx_sos_volunteer       ON sos_beacons (assigned_volunteer_id);
CREATE INDEX idx_sos_created_at      ON sos_beacons (created_at);
CREATE INDEX idx_users_role          ON users (role);
CREATE INDEX idx_users_volunteer_st  ON users (volunteer_status);
CREATE INDEX idx_history_sos_id      ON sos_status_history (sos_id);
CREATE INDEX idx_history_changed_at  ON sos_status_history (changed_at);