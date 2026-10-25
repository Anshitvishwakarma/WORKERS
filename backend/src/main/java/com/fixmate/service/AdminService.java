package com.fixmate.service;

import com.fixmate.dto.AdminDashboardDto;
import com.fixmate.dto.BookingDto;
import com.fixmate.dto.UserSummaryDto;
import com.fixmate.enums.Role;

import java.util.List;

public interface AdminService {
    AdminDashboardDto getDashboardStats();
    List<UserSummaryDto> getUsers(Role role, String search);
    UserSummaryDto toggleUserStatus(Long userId, boolean active);
    List<BookingDto> getAllBookings();
}
