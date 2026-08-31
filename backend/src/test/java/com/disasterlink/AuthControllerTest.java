package com.disasterlink;

import com.disasterlink.dto.LoginRequest;
import com.disasterlink.dto.RegisterRequest;
import com.disasterlink.entity.Role;
import com.disasterlink.repository.UserRepository;
import com.disasterlink.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByUsername("testvictim")) {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("testvictim");
            req.setEmail("victim@test.com");
            req.setPassword("password123");
            req.setRole(Role.VICTIM);
            authService.register(req);
        }
    }

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("testvictim");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testvictim"))
                .andExpect(jsonPath("$.role").value("VICTIM"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("testvictim");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
