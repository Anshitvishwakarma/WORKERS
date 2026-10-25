package com.fixmate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixmate.dto.BookingCreateRequest;
import com.fixmate.entity.ProviderService;
import com.fixmate.entity.User;
import com.fixmate.enums.Role;
import com.fixmate.repository.ProviderServiceRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookingWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderServiceRepository providerServiceRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Complete booking lifecycle: create, accept, complete, and IDOR protection")
    void testCompleteBookingLifecycle() throws Exception {
        // Users
        User customer = userRepository.findByEmailIgnoreCase("customer@fixmate.com").orElseThrow();
        String customerToken = jwtUtil.generateToken(customer);

        User provider = userRepository.findByEmailIgnoreCase("plumber@fixmate.com").orElseThrow();
        String providerToken = jwtUtil.generateToken(provider);

        User otherProvider = userRepository.findByEmailIgnoreCase("electric@fixmate.com").orElseThrow();
        String otherProviderToken = jwtUtil.generateToken(otherProvider);

        // Get an active provider service for plumber
        ProviderService ps = providerServiceRepository.findByProviderId(provider.getId()).getFirst();

        // 1. Customer creates a booking
        BookingCreateRequest createRequest = new BookingCreateRequest(
                ps.getId(),
                LocalDate.now().plusDays(5),
                LocalTime.of(11, 0),
                "100 Sunset Blvd, Apt 4B",
                "Fix bathroom pipe dripping"
        );

        String createResponse = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.price", is(ps.getPrice().doubleValue()))) // Server-verified price!
                .andExpect(jsonPath("$.customerEmail", is(customer.getEmail())))
                .andReturn().getResponse().getContentAsString();

        Number bookingIdNum = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");
        Long bookingId = bookingIdNum.longValue();

        // 2. IDOR Prevention: Other provider cannot accept this booking
        mockMvc.perform(patch("/api/provider/bookings/" + bookingId + "/accept")
                        .header("Authorization", "Bearer " + otherProviderToken))
                .andExpect(status().isForbidden());

        // 3. Provider views their bookings
        mockMvc.perform(get("/api/provider/bookings")
                        .header("Authorization", "Bearer " + providerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        // 4. Assigned provider accepts the booking
        mockMvc.perform(patch("/api/provider/bookings/" + bookingId + "/accept")
                        .header("Authorization", "Bearer " + providerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")));

        // 5. Assigned provider completes the booking
        mockMvc.perform(patch("/api/provider/bookings/" + bookingId + "/complete")
                        .header("Authorization", "Bearer " + providerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        // 6. Invalid transition: Customer cannot cancel a COMPLETED booking
        mockMvc.perform(patch("/api/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Completed bookings cannot be cancelled")));
    }

    @Test
    @DisplayName("Customer cancels their own eligible pending booking")
    void testCustomerCancelBooking() throws Exception {
        User customer = userRepository.findByEmailIgnoreCase("customer@fixmate.com").orElseThrow();
        String customerToken = jwtUtil.generateToken(customer);

        User provider = userRepository.findByEmailIgnoreCase("plumber@fixmate.com").orElseThrow();
        ProviderService ps = providerServiceRepository.findByProviderId(provider.getId()).getFirst();

        // Create booking
        BookingCreateRequest createRequest = new BookingCreateRequest(
                ps.getId(),
                LocalDate.now().plusDays(4),
                LocalTime.of(15, 0),
                "742 Evergreen Terrace",
                "Kitchen sink drain clog"
        );

        String createResponse = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number bookingIdNum = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");
        Long bookingId = bookingIdNum.longValue();

        // Cancel booking
        mockMvc.perform(patch("/api/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }
}
