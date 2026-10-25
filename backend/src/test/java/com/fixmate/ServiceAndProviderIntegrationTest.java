package com.fixmate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixmate.dto.ProviderServiceCreateRequest;
import com.fixmate.dto.ProviderServiceUpdateRequest;
import com.fixmate.entity.Service;
import com.fixmate.entity.User;
import com.fixmate.repository.ServiceRepository;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ServiceAndProviderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Should search services by keyword")
    void shouldSearchServices() throws Exception {
        mockMvc.perform(get("/api/services/search").param("name", "plumb"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", containsStringIgnoringCase("Plumbing")));
    }

    @Test
    @DisplayName("Provider should successfully manage their service offerings")
    void shouldAllowProviderToManageOfferings() throws Exception {
        User provider = userRepository.findByEmailIgnoreCase("electric@fixmate.com").orElseThrow();
        String providerToken = jwtUtil.generateToken(provider);

        // Find a service that provider doesn't have yet (e.g. Painting or Carpenter)
        Service painting = serviceRepository.findByNameIgnoreCase("Painting").orElseThrow();

        // 1. Add offering
        ProviderServiceCreateRequest createRequest = new ProviderServiceCreateRequest(
                painting.getId(),
                new BigDecimal("120.00"),
                4,
                "Weekdays 9AM-5PM"
        );

        String responseJson = mockMvc.perform(post("/api/providers/me/services")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceName", is("Painting")))
                .andExpect(jsonPath("$.price", is(120.00)))
                .andReturn().getResponse().getContentAsString();

        Number psIdNum = com.jayway.jsonpath.JsonPath.read(responseJson, "$.id");
        Long providerServiceId = psIdNum.longValue();

        // 2. Reject duplicate offering
        mockMvc.perform(post("/api/providers/me/services")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        // 3. Update offering
        ProviderServiceUpdateRequest updateRequest = new ProviderServiceUpdateRequest(
                new BigDecimal("135.00"),
                5,
                "Mon-Fri 8AM-4PM"
        );

        mockMvc.perform(put("/api/providers/me/services/" + providerServiceId)
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(135.00)))
                .andExpect(jsonPath("$.experienceYears", is(5)));

        // 4. View provider's own offerings
        mockMvc.perform(get("/api/providers/me/services")
                        .header("Authorization", "Bearer " + providerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }
}
