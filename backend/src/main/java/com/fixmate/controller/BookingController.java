package com.fixmate.controller;

import com.fixmate.dto.BookingCreateRequest;
import com.fixmate.dto.BookingDto;
import com.fixmate.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Bookings", description = "Endpoints for customer booking creation and provider booking workflow")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // =========================================================================
    // Customer Endpoints
    // =========================================================================

    @PostMapping("/api/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new booking (Customer Only)")
    public ResponseEntity<BookingDto> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookingCreateRequest request) {
        BookingDto created = bookingService.createBooking(userDetails.getUsername(), request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/api/bookings/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get booking history for logged-in customer (Customer Only)")
    public ResponseEntity<List<BookingDto>> getMyBookings(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getCustomerBookings(userDetails.getUsername()));
    }

    @GetMapping("/api/bookings/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Get booking details by ID (Customer / Admin)")
    public ResponseEntity<BookingDto> getBookingById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingDetails(userDetails.getUsername(), id));
    }

    @PatchMapping("/api/bookings/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel an eligible booking (Customer Only)")
    public ResponseEntity<BookingDto> cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(userDetails.getUsername(), id));
    }

    // =========================================================================
    // Provider Endpoints
    // =========================================================================

    @GetMapping("/api/provider/bookings")
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Get bookings assigned to logged-in provider (Provider Only)")
    public ResponseEntity<List<BookingDto>> getProviderBookings(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getProviderBookings(userDetails.getUsername()));
    }

    @PatchMapping("/api/provider/bookings/{id}/accept")
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Accept a pending booking (Provider Only)")
    public ResponseEntity<BookingDto> acceptBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.acceptBooking(userDetails.getUsername(), id));
    }

    @PatchMapping("/api/provider/bookings/{id}/reject")
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Reject a pending booking (Provider Only)")
    public ResponseEntity<BookingDto> rejectBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.rejectBooking(userDetails.getUsername(), id));
    }

    @PatchMapping("/api/provider/bookings/{id}/complete")
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Mark an accepted booking as completed (Provider Only)")
    public ResponseEntity<BookingDto> completeBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.completeBooking(userDetails.getUsername(), id));
    }
}
