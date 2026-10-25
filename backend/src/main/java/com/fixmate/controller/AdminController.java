package com.fixmate.controller;

import com.fixmate.dto.*;
import com.fixmate.enums.Role;
import com.fixmate.service.AdminService;
import com.fixmate.service.ServiceCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administrator Management", description = "Admin-only operations for platform control, users, services, and bookings")
public class AdminController {

    private final AdminService adminService;
    private final ServiceCatalogService serviceCatalogService;

    public AdminController(AdminService adminService, ServiceCatalogService serviceCatalogService) {
        this.adminService = adminService;
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get platform overview KPIs and statistics (Admin Only)")
    public ResponseEntity<AdminDashboardDto> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    @Operation(summary = "List users with optional role and search filter (Admin Only)")
    public ResponseEntity<List<UserSummaryDto>> getUsers(
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(adminService.getUsers(role, search));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Activate or deactivate a user account (Admin Only)")
    public ResponseEntity<UserSummaryDto> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam(name = "active") boolean active) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id, active));
    }

    @GetMapping("/services")
    @Operation(summary = "Get all platform services including inactive (Admin Only)")
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        return ResponseEntity.ok(serviceCatalogService.getAllServices());
    }

    @PostMapping("/services")
    @Operation(summary = "Create a new service category (Admin Only)")
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceCreateRequest request) {
        ServiceDto created = serviceCatalogService.createService(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/services/{id}")
    @Operation(summary = "Update service details (Admin Only)")
    public ResponseEntity<ServiceDto> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceUpdateRequest request) {
        return ResponseEntity.ok(serviceCatalogService.updateService(id, request));
    }

    @PatchMapping("/services/{id}/status")
    @Operation(summary = "Activate or deactivate a service (Admin Only)")
    public ResponseEntity<ServiceDto> toggleServiceStatus(
            @PathVariable Long id,
            @RequestParam(name = "active") boolean active) {
        return ResponseEntity.ok(serviceCatalogService.toggleServiceStatus(id, active));
    }

    @DeleteMapping("/services/{id}")
    @Operation(summary = "Deactivate/delete a service safely (Admin Only)")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        serviceCatalogService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.message("Service deactivated successfully"));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get all customer bookings across the platform (Admin Only)")
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        return ResponseEntity.ok(adminService.getAllBookings());
    }
}
