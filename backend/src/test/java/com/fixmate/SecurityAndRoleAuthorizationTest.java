package com.fixmate;

import com.fixmate.entity.User;
import com.fixmate.repository.UserRepository;
import com.fixmate.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityAndRoleAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Public service listing should work without any authentication token")
    void shouldAllowPublicAccessToServices() throws Exception {
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Unauthenticated request to protected admin endpoint must return 401 Unauthorized")
    void shouldReturn401ForUnauthenticatedAdminAccess() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("Customer role accessing admin endpoint must return 403 Forbidden")
    void shouldReturn403WhenCustomerAccessesAdmin() throws Exception {
        User customer = userRepository.findByEmailIgnoreCase("customer@fixmate.com").orElseThrow();
        String customerToken = jwtUtil.generateToken(customer);

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("Customer role accessing provider service management must return 403 Forbidden")
    void shouldReturn403WhenCustomerAccessesProviderEndpoints() throws Exception {
        User customer = userRepository.findByEmailIgnoreCase("customer@fixmate.com").orElseThrow();
        String customerToken = jwtUtil.generateToken(customer);

        mockMvc.perform(get("/api/providers/me/services")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("Provider role accessing admin endpoints must return 403 Forbidden")
    void shouldReturn403WhenProviderAccessesAdmin() throws Exception {
        User provider = userRepository.findByEmailIgnoreCase("plumber@fixmate.com").orElseThrow();
        String providerToken = jwtUtil.generateToken(provider);

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + providerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("Admin role should successfully access admin dashboard")
    void shouldAllowAdminAccessToDashboard() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@fixmate.com").orElseThrow();
        String adminToken = jwtUtil.generateToken(admin);

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalServices", greaterThanOrEqualTo(1)));
    }
}
