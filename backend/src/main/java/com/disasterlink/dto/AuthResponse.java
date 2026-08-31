package com.disasterlink.dto;

import com.disasterlink.entity.Role;

/**
 * Returned by /api/auth/register and /api/auth/login.
 * Contains the JWT token and basic user information for the frontend to store.
 *
 * FIX M1: Converted to a Java Record.
 * Records are the modern Java 16+ (and idiomatic Java 21) way to define immutable
 * data carriers. They auto-generate: constructor, getters (as accessor methods),
 * equals(), hashCode(), and toString() — eliminating ~40 lines of boilerplate.
 * Jackson serializes records correctly since Jackson 2.12+.
 */
public record AuthResponse(
        String token,
        Long userId,
        String username,
        String email,
        Role role
) {}