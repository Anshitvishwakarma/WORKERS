package com.fixmate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixmate.dto.AuthRequest;
import com.fixmate.dto.RegisterRequest;
import com.fixmate.entity.User;
import com.fixmate.enums.Role;
import com.fixmate.repository.UserRepository;
import com.fixmate.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Should successfully register a new customer")
    void shouldRegisterCustomerSuccessfully() throws Exception {
        String email = "new_customer_" + System.currentTimeMillis() + "@test.com";
        RegisterRequest request = new RegisterRequest(
                "Jane Doe",
                email,
                "+1-555-0987",
                "123 Maple Street",
                "Password123",
                Role.CUSTOMER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.email", is(email)))
                .andExpect(jsonPath("$.user.role", is("CUSTOMER")));

        // Verify password hashing in database
        User saved = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("Password123");
        assertThat(passwordEncoder.matches("Password123", saved.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Should successfully register a new provider")
    void shouldRegisterProviderSuccessfully() throws Exception {
        String email = "new_provider_" + System.currentTimeMillis() + "@test.com";
        RegisterRequest request = new RegisterRequest(
                "Bob Builder",
                email,
                "+1-555-0988",
                "456 Oak Avenue",
                "ProviderPass123",
                Role.PROVIDER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role", is("PROVIDER")));
    }

    @Test
    @DisplayName("Should reject public registration with ADMIN role")
    void shouldRejectAdminRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Hacker Admin",
                "hacker_admin@test.com",
                "+1-555-0000",
                "Unknown Dark Alley",
                "AdminHacked123",
                Role.ADMIN
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("cannot be created via public registration")));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void shouldRejectDuplicateEmailRegistration() throws Exception {
        String email = "duplicate_" + System.currentTimeMillis() + "@test.com";
        RegisterRequest request1 = new RegisterRequest(
                "User One", email, "+1-555-1111", "Address 1", "Pass1234", Role.CUSTOMER
        );
        RegisterRequest request2 = new RegisterRequest(
                "User Two", email, "+1-555-2222", "Address 2", "Pass1234", Role.CUSTOMER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Should successfully login and return JWT")
    void shouldLoginSuccessfully() throws Exception {
        AuthRequest request = new AuthRequest("customer@fixmate.com", "Customer@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email", is("customer@fixmate.com")))
                .andExpect(jsonPath("$.user.role", is("CUSTOMER")));
    }

    @Test
    @DisplayName("Should reject login with incorrect password")
    void shouldRejectInvalidCredentials() throws Exception {
        AuthRequest request = new AuthRequest("customer@fixmate.com", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid email or password.")));
    }

    @Test
    @DisplayName("Should validate /api/auth/me with valid Bearer token")
    void shouldGetMeProfileWithValidToken() throws Exception {
        User customer = userRepository.findByEmailIgnoreCase("customer@fixmate.com").orElseThrow();
        String token = jwtUtil.generateToken(customer);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("customer@fixmate.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }

    @Test
    @DisplayName("Should return 401 when accessing /api/auth/me with invalid or expired token")
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.fake.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }
}
