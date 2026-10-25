package com.fixmate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixmate.dto.ServiceCreateRequest;
import com.fixmate.dto.ServiceUpdateRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Admin retrieves platform dashboard KPIs")
    void shouldReturnDashboardMetrics() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@fixmate.com").orElseThrow();
        String adminToken = jwtUtil.generateToken(admin);

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalCustomers", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalProviders", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalServices", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Admin lists users and filters by role")
    void shouldListAndFilterUsers() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@fixmate.com").orElseThrow();
        String adminToken = jwtUtil.generateToken(admin);

        // Filter by role CUSTOMER
        mockMvc.perform(get("/api/admin/users")
                        .param("role", "CUSTOMER")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].role", is("CUSTOMER")));
    }

    @Test
    @DisplayName("Admin toggles user status")
    void shouldToggleUserStatus() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@fixmate.com").orElseThrow();
        String adminToken = jwtUtil.generateToken(admin);

        User customer = userRepository.findByEmailIgnoreCase("customer@fixmate.com").orElseThrow();

        // Deactivate customer
        mockMvc.perform(patch("/api/admin/users/" + customer.getId() + "/status")
                        .param("active", "false")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));

        // Reactivate customer
        mockMvc.perform(patch("/api/admin/users/" + customer.getId() + "/status")
                        .param("active", "true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("Admin creates and updates a service")
    void shouldCreateAndUpdateService() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@fixmate.com").orElseThrow();
        String adminToken = jwtUtil.generateToken(admin);

        String serviceName = "Pest Control " + System.currentTimeMillis();
        ServiceCreateRequest createReq = new ServiceCreateRequest(
                serviceName,
                "Professional termite, bedbug, and rodent extermination.",
                "bi-bug"
        );

        String response = mockMvc.perform(post("/api/admin/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(serviceName)))
                .andReturn().getResponse().getContentAsString();

        Number sIdNum = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        Long serviceId = sIdNum.longValue();

        // Update service
        ServiceUpdateRequest updateReq = new ServiceUpdateRequest(
                serviceName + " Premium",
                "Updated description with 100% organic treatments.",
                "bi-shield-check",
                true
        );

        mockMvc.perform(put("/api/admin/services/" + serviceId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(serviceName + " Premium")))
                .andExpect(jsonPath("$.icon", is("bi-shield-check")));
    }
}
