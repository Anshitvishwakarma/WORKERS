package com.fixmate.service.impl;

import com.fixmate.dto.AdminDashboardDto;
import com.fixmate.dto.BookingDto;
import com.fixmate.dto.UserSummaryDto;
import com.fixmate.entity.User;
import com.fixmate.enums.BookingStatus;
import com.fixmate.enums.Role;
import com.fixmate.exception.BadRequestException;
import com.fixmate.exception.ResourceNotFoundException;
import com.fixmate.mapper.EntityMapper;
import com.fixmate.repository.BookingRepository;
import com.fixmate.repository.ServiceRepository;
import com.fixmate.repository.UserRepository;
import com.fixmate.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final EntityMapper entityMapper;

    public AdminServiceImpl(UserRepository userRepository,
                            ServiceRepository serviceRepository,
                            BookingRepository bookingRepository,
                            EntityMapper entityMapper) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.bookingRepository = bookingRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalProviders = userRepository.countByRole(Role.PROVIDER);
        long totalServices = serviceRepository.count();

        long totalBookings = bookingRepository.count();
        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        long acceptedBookings = bookingRepository.countByStatus(BookingStatus.ACCEPTED);
        long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        BigDecimal totalRevenue = bookingRepository.calculateTotalCompletedRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        return new AdminDashboardDto(
                totalUsers,
                totalCustomers,
                totalProviders,
                totalServices,
                totalBookings,
                pendingBookings,
                acceptedBookings,
                completedBookings,
                cancelledBookings,
                totalRevenue
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getUsers(Role role, String search) {
        String trimmedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        return userRepository.searchUsers(role, trimmedSearch)
                .stream()
                .map(entityMapper::toUserSummaryDto)
                .toList();
    }

    @Override
    @Transactional
    public UserSummaryDto toggleUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Safety check: Don't allow deactivating all admins or the primary admin
        if (user.getRole() == Role.ADMIN && !active) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot deactivate the only active platform administrator.");
            }
        }

        user.setActive(active);
        User updated = userRepository.save(user);
        return entityMapper.toUserSummaryDto(updated);
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
