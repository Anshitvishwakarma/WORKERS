package com.fixmate.controller;

import com.fixmate.dto.ServiceDto;
import com.fixmate.service.ServiceCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@Tag(name = "Services Catalog", description = "Public endpoints for exploring services")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    @Operation(summary = "Get all active services")
    public ResponseEntity<List<ServiceDto>> getAllActiveServices() {
        return ResponseEntity.ok(serviceCatalogService.getActiveServices());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service details by ID")
    public ResponseEntity<ServiceDto> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceCatalogService.getServiceById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search services by name or description keyword")
    public ResponseEntity<List<ServiceDto>> searchServices(@RequestParam(name = "name", required = false) String name) {
        return ResponseEntity.ok(serviceCatalogService.searchServices(name));
    }
}
