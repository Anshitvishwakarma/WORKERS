package com.fixmate.dto;

import java.math.BigDecimal;

public class AdminDashboardDto {

    private long totalUsers;
    private long totalCustomers;
    private long totalProviders;
    private long totalServices;
    private long totalBookings;
    private long pendingBookings;
    private long acceptedBookings;
    private long completedBookings;
    private long cancelledBookings;
    private BigDecimal totalRevenue;

    public AdminDashboardDto() {
    }

    public AdminDashboardDto(long totalUsers, long totalCustomers, long totalProviders,
                             long totalServices, long totalBookings, long pendingBookings,
                             long acceptedBookings, long completedBookings, long cancelledBookings,
                             BigDecimal totalRevenue) {
        this.totalUsers = totalUsers;
        this.totalCustomers = totalCustomers;
        this.totalProviders = totalProviders;
        this.totalServices = totalServices;
        this.totalBookings = totalBookings;
        this.pendingBookings = pendingBookings;
        this.acceptedBookings = acceptedBookings;
        this.completedBookings = completedBookings;
        this.cancelledBookings = cancelledBookings;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalProviders() {
        return totalProviders;
    }

    public void setTotalProviders(long totalProviders) {
        this.totalProviders = totalProviders;
    }

    public long getTotalServices() {
        return totalServices;
    }

    public void setTotalServices(long totalServices) {
        this.totalServices = totalServices;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getPendingBookings() {
        return pendingBookings;
    }

    public void setPendingBookings(long pendingBookings) {
        this.pendingBookings = pendingBookings;
    }

    public long getAcceptedBookings() {
        return acceptedBookings;
    }

    public void setAcceptedBookings(long acceptedBookings) {
        this.acceptedBookings = acceptedBookings;
    }

    public long getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(long completedBookings) {
        this.completedBookings = completedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
