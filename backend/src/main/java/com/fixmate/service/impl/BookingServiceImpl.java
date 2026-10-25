package com.fixmate.service.impl;

import com.fixmate.dto.BookingCreateRequest;
import com.fixmate.dto.BookingDto;
import com.fixmate.entity.Booking;
import com.fixmate.entity.ProviderService;
import com.fixmate.entity.User;
import com.fixmate.enums.BookingStatus;
import com.fixmate.enums.Role;
import com.fixmate.exception.BadRequestException;
import com.fixmate.exception.ForbiddenException;
import com.fixmate.exception.ResourceNotFoundException;
import com.fixmate.mapper.EntityMapper;
import com.fixmate.repository.BookingRepository;
import com.fixmate.repository.ProviderServiceRepository;
import com.fixmate.repository.UserRepository;
import com.fixmate.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ProviderServiceRepository providerServiceRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ProviderServiceRepository providerServiceRepository,
                              UserRepository userRepository,
                              EntityMapper entityMapper) {
        this.bookingRepository = bookingRepository;
        this.providerServiceRepository = providerServiceRepository;
        this.userRepository = userRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional
    public BookingDto createBooking(String customerEmail, BookingCreateRequest request) {
        User customer = userRepository.findByEmailIgnoreCase(customerEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerEmail));

        if (customer.getRole() != Role.CUSTOMER) {
            throw new ForbiddenException("Only customers can book home services.");
        }

        if (!customer.isActive()) {
            throw new BadRequestException("Your customer account is currently inactive.");
        }

        ProviderService ps = providerServiceRepository.findByIdWithDetails(request.getProviderServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider Service", "id", request.getProviderServiceId()));

        // Validate provider active status
        if (!ps.getProvider().isActive()) {
            throw new BadRequestException("This service provider is currently inactive and cannot accept bookings.");
        }

        // Validate service active status
        if (!ps.getService().isActive()) {
            throw new BadRequestException("This service is currently unavailable in the platform catalog.");
        }

        // Customer cannot book their own service if they happen to be the provider
        if (customer.getId().equals(ps.getProvider().getId())) {
            throw new BadRequestException("You cannot book your own service offering.");
        }

        // Date and time validation
        LocalDate bookingDate = request.getBookingDate();
        LocalTime bookingTime = request.getBookingTime();
        LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingTime);

        if (bookingDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Booking date and time cannot be in the past.");
        }

        // Server-side price calculation / verification
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setProviderService(ps);
        booking.setBookingDate(bookingDate);
        booking.setBookingTime(bookingTime);
        booking.setServiceAddress(request.getServiceAddress().trim());
        booking.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        booking.setPrice(ps.getPrice()); // Always source authoritative price from database
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        return entityMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getCustomerBookings(String customerEmail) {
        User customer = userRepository.findByEmailIgnoreCase(customerEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerEmail));

        return bookingRepository.findByCustomerIdWithDetails(customer.getId())
                .stream()
                .map(entityMapper::toBookingDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingDetails(String userEmail, Long bookingId) {
        User user = userRepository.findByEmailIgnoreCase(userEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Ownership & Role Verification (IDOR prevention)
        boolean isCustomer = booking.getCustomer().getId().equals(user.getId());
        boolean isProvider = booking.getProviderService().getProvider().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isCustomer && !isProvider && !isAdmin) {
            throw new ForbiddenException("You are not authorized to view this booking.");
        }

        return entityMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto cancelBooking(String customerEmail, Long bookingId) {
        User customer = userRepository.findByEmailIgnoreCase(customerEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerEmail));

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Customer IDOR check
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You can only cancel your own bookings.");
        }

        // Allowed transitions: PENDING or ACCEPTED -> CANCELLED
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Completed bookings cannot be cancelled.");
        }
        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new BadRequestException("This booking was already rejected by the provider.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("This booking is already cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);
        return entityMapper.toBookingDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getProviderBookings(String providerEmail) {
        User provider = userRepository.findByEmailIgnoreCase(providerEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + providerEmail));

        if (provider.getRole() != Role.PROVIDER) {
            throw new ForbiddenException("Only service providers can view provider bookings.");
        }

        return bookingRepository.findByProviderIdWithDetails(provider.getId())
                .stream()
                .map(entityMapper::toBookingDto)
                .toList();
    }

    @Override
    @Transactional
    public BookingDto acceptBooking(String providerEmail, Long bookingId) {
        User provider = userRepository.findByEmailIgnoreCase(providerEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + providerEmail));

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getProviderService().getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("You can only manage bookings assigned to you.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be accepted. Current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.ACCEPTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return entityMapper.toBookingDto(updatedBooking);
    }

    @Override
    @Transactional
    public BookingDto rejectBooking(String providerEmail, Long bookingId) {
        User provider = userRepository.findByEmailIgnoreCase(providerEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + providerEmail));

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getProviderService().getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("You can only manage bookings assigned to you.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be rejected. Current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return entityMapper.toBookingDto(updatedBooking);
    }

    @Override
    @Transactional
    public BookingDto completeBooking(String providerEmail, Long bookingId) {
        User provider = userRepository.findByEmailIgnoreCase(providerEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + providerEmail));

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getProviderService().getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("You can only manage bookings assigned to you.");
        }

        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new BadRequestException("Only ACCEPTED bookings can be marked as COMPLETED. Current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);
        return entityMapper.toBookingDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAllWithDetails()
                .stream()
                .map(entityMapper::toBookingDto)
                .toList();
    }
}
