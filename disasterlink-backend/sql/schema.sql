-- DisasterLink - Database Schema (MySQL)
-- NOTE: This file is for reference only. Hibernate will automatically
-- create/update these tables at startup because of:
--   spring.jpa.hibernate.ddl-auto=update
-- You do NOT need to run this script manually unless you want to
-- set up the schema by hand or inspect the expected structure.

CREATE DATABASE IF NOT EXISTS disasterlink_db;
USE disasterlink_db;

-- ========================
-- Table: users
-- ========================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    created_at  DATETIME     NOT NULL
);

-- ========================
-- Table: disaster_reports
-- ========================
CREATE TABLE IF NOT EXISTS disaster_reports (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(100)  NOT NULL,
    description    VARCHAR(1000) NOT NULL,
    location       VARCHAR(150)  NOT NULL,
    disaster_type  VARCHAR(20)   NOT NULL, -- FLOOD, FIRE, EARTHQUAKE, CYCLONE, LANDSLIDE, OTHER
    status         VARCHAR(20)   NOT NULL, -- PENDING, IN_PROGRESS, RESOLVED
    user_id        BIGINT        NOT NULL,
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NOT NULL,
    CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES users(id)
);
