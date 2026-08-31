package com.disasterlink.service;

import com.disasterlink.dto.AuthResponse;
import com.disasterlink.dto.LoginRequest;
import com.disasterlink.dto.RegisterRequest;
import com.disasterlink.entity.Role;
import com.disasterlink.entity.User;
import com.disasterlink.entity.VolunteerStatus;
import com.disasterlink.exception.DuplicateResourceException;
import com.disasterlink.exception.InvalidCredentialsException;
import com.disasterlink.exception.UnauthorizedException;
import com.disasterlink.repository.UserRepository;
import com.disasterlink.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and login.
 *
 * Registration rules:
 * - Self-registration is allowed for VICTIM and VOLUNTEER only.
 * - OFFICER accounts are created by a database administrator directly.
 * - When a VOLUNTEER registers, volunteerStatus is set to AVAILABLE.
 *
 * FIX L3: @Transactional added to register().
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil         = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Officers cannot self-register
        if (request.getRole() == Role.OFFICER) {
            throw new UnauthorizedException("Officer accounts must be created by an administrator");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

        // Set volunteer availability status on registration
        if (request.getRole() == Role.VOLUNTEER) {
            user.setVolunteerStatus(VolunteerStatus.AVAILABLE);
        }

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getUsername(), saved.getRole());
        return new AuthResponse(token, saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}