package com.fixmate.controller;

import com.fixmate.dto.ApiResponse;
import com.fixmate.dto.ProviderServiceCreateRequest;
import com.fixmate.dto.ProviderServiceDto;
import com.fixmate.dto.ProviderServiceUpdateRequest;
import com.fixmate.service.ProviderServiceService;
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
@RequestMapping("/api/providers")
@Tag(name = "Providers & Service Offerings", description = "Endpoints for finding providers and managing provider service offerings")
public class ProviderController {

    private final ProviderServiceService providerServiceService;

    public ProviderController(ProviderServiceService providerServiceService) {
        this.providerServiceService = providerServiceService;
    }

    @GetMapping
    @Operation(summary = "Get all active service providers and offerings (Public)")
    public ResponseEntity<List<ProviderServiceDto>> getAllProviders() {
        return ResponseEntity.ok(providerServiceService.getAllActiveProviders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get provider service offering by ID (Public)")
    public ResponseEntity<ProviderServiceDto> getProviderById(@PathVariable Long id) {
        return ResponseEntity.ok(providerServiceService.getProviderServiceById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Find providers offering a specific service ID (Public)")
    public ResponseEntity<List<ProviderServiceDto>> searchProvidersByService(@RequestParam(name = "serviceId") Long serviceId) {
        return ResponseEntity.ok(providerServiceService.getProvidersByServiceId(serviceId));
    }

    @GetMapping("/me/services")
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Get all services offered by logged-in provider (Provider Only)")
    public ResponseEntity<List<ProviderServiceDto>> getMyServices(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(providerServiceService.getMyServices(userDetails.getUsername()));
    }

    @PostMapping("/me/services")
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Add a service offering to provider's profile (Provider Only)")
    public ResponseEntity<ProviderServiceDto> addService(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProviderServiceCreateRequest request) {
        ProviderServiceDto created = providerServiceService.addServiceOffering(userDetails.getUsername(), request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/me/services/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Update a service offering (Provider Only)")
    public ResponseEntity<ProviderServiceDto> updateService(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ProviderServiceUpdateRequest request) {
        ProviderServiceDto updated = providerServiceService.updateServiceOffering(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/me/services/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Delete/remove a service offering (Provider Only)")
    public ResponseEntity<ApiResponse<Void>> deleteService(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        providerServiceService.removeServiceOffering(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.message("Service offering removed successfully"));
    }
}
