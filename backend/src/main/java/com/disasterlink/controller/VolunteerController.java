package com.disasterlink.controller;

import com.disasterlink.entity.Role;
import com.disasterlink.entity.User;
import com.disasterlink.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Provides a list of volunteers for the officer assignment dropdown.
 * Only accessible to officers (enforced in SecurityConfig).
 */
@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {

    private final UserRepository userRepository;

    public VolunteerController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Returns all users with the VOLUNTEER role (id + username only). */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getVolunteers() {
        List<User> volunteers = userRepository.findAllByRole(Role.VOLUNTEER);
        // FIX L1: Use Java 16+ Stream.toList() instead of Collectors.toList()
        List<Map<String, Object>> result = volunteers.stream()
                .map(v -> Map.of("id", (Object) v.getId(), "username", v.getUsername()))
                .toList();
        return ResponseEntity.ok(result);
    }
}