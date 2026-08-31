package com.disasterlink.entity;

/**
 * @deprecated Replaced by {@link SosBeacon} in v2.0.
 * Must remain PUBLIC so existing DTOs and repositories still compile.
 * No JPA annotations — Hibernate will NOT create a table for this.
 */
@Deprecated
public class DisasterReport {}
