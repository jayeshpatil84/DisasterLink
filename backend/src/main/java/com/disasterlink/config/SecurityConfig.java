package com.disasterlink.config;

import com.disasterlink.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for the stateless, JWT-based REST API.
 *
 * Access rules:
 *   /api/auth/**            → public (register / login)
 *   /ws/**                  → public (WebSocket handshake does not carry JWT)
 *   /actuator/health        → public (Docker health checks)
 *   /api/officer/**         → OFFICER only
 *   /api/volunteer/**       → VOLUNTEER only
 *   /api/victim/**          → VICTIM only
 *   /api/sos  (POST)        → VICTIM, VOLUNTEER, OFFICER
 *   /api/sos  (GET)         → VOLUNTEER, OFFICER
 *   /api/sos/my             → VICTIM
 *   /api/sos/assigned       → VOLUNTEER, OFFICER
 *   /api/sos/{id}/assign    → OFFICER only
 *   /api/sos/{id}/reassign  → OFFICER only
 *   /api/volunteers         → OFFICER only
 *   /api/dashboard/**       → VOLUNTEER, OFFICER
 *   everything else         → authenticated
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableAsync
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOriginsRaw;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/ws/**").permitAll()           // WebSocket handshake
                .requestMatchers("/actuator/health").permitAll() // Docker health check

                // Officer endpoints
                .requestMatchers("/api/officer/**").hasRole("OFFICER")
                .requestMatchers("/api/volunteers/**").hasRole("OFFICER")
                .requestMatchers("/api/sos/*/assign").hasRole("OFFICER")
                .requestMatchers("/api/sos/*/reassign").hasRole("OFFICER")

                // Volunteer endpoints
                .requestMatchers("/api/volunteer/**").hasRole("VOLUNTEER")

                // Victim endpoints
                .requestMatchers("/api/victim/**").hasRole("VICTIM")
                .requestMatchers("/api/sos/my").hasRole("VICTIM")

                // Shared SOS endpoints
                .requestMatchers("POST", "/api/sos").hasAnyRole("VICTIM", "VOLUNTEER", "OFFICER")
                .requestMatchers("GET", "/api/sos").hasAnyRole("VOLUNTEER", "OFFICER")
                .requestMatchers("/api/sos/assigned").hasAnyRole("VOLUNTEER", "OFFICER")
                .requestMatchers("/api/dashboard/**").hasAnyRole("VOLUNTEER", "OFFICER")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}