package com.fixmate.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class BookingCreateRequest {

    @NotNull(message = "Provider service ID is required")
    private Long providerServiceId;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date cannot be in the past")
    private LocalDate bookingDate;

    @NotNull(message = "Booking time is required")
    private LocalTime bookingTime;

    @NotBlank(message = "Service address is required")
    private String serviceAddress;

    private String description;

    public BookingCreateRequest() {
    }

    public BookingCreateRequest(Long providerServiceId, LocalDate bookingDate, LocalTime bookingTime,
                                String serviceAddress, String description) {
        this.providerServiceId = providerServiceId;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.serviceAddress = serviceAddress;
        this.description = description;
    }

    public Long getProviderServiceId() {
        return providerServiceId;
    }

    public void setProviderServiceId(Long providerServiceId) {
        this.providerServiceId = providerServiceId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
