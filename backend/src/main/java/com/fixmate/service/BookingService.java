package com.fixmate.service;

import com.fixmate.dto.BookingCreateRequest;
import com.fixmate.dto.BookingDto;

import java.util.List;

public interface BookingService {

    // Customer operations
    BookingDto createBooking(String customerEmail, BookingCreateRequest request);
    List<BookingDto> getCustomerBookings(String customerEmail);
    BookingDto getBookingDetails(String userEmail, Long bookingId);
    BookingDto cancelBooking(String customerEmail, Long bookingId);

    // Provider operations
    List<BookingDto> getProviderBookings(String providerEmail);
    BookingDto acceptBooking(String providerEmail, Long bookingId);
    BookingDto rejectBooking(String providerEmail, Long bookingId);
    BookingDto completeBooking(String providerEmail, Long bookingId);

    // Admin operations
    List<BookingDto> getAllBookings();
}
