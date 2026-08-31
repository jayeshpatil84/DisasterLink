package com.disasterlink.security;

import com.disasterlink.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Generates and validates JWT tokens.
 *
 * Token claims:
 *   - sub     → username
 *   - userId  → database user ID
 *   - role    → VICTIM | VOLUNTEER | OFFICER
 *   - iat     → issued at
 *   - exp     → expiry
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private volatile SecretKey signingKey;

    private SecretKey getSigningKey() {
        SecretKey key = this.signingKey;
        if (key == null) {
            synchronized (this) {
                key = this.signingKey;
                if (key == null) {
                    if (secret == null || secret.getBytes().length < 32) {
                        throw new IllegalStateException(
                                "jwt.secret must be at least 32 characters. Set JWT_SECRET in the environment.");
                    }
                    key = Keys.hmacShaKeyFor(secret.getBytes());
                    this.signingKey = key;
                }
            }
        }
        return key;
    }

    /** Creates a signed JWT containing the user's ID, username, and role. */
    public String generateToken(Long userId, String username, Role role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Role extractRole(String token) {
        String roleName = parseClaims(token).get("role", String.class);
        return Role.valueOf(roleName);
    }

    public boolean isTokenValid(String token) {
        try {
            return parseClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
