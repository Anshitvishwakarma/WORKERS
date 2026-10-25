package com.fixmate.service;

import com.fixmate.dto.ServiceCreateRequest;
import com.fixmate.dto.ServiceDto;
import com.fixmate.dto.ServiceUpdateRequest;

import java.util.List;

public interface ServiceCatalogService {
    List<ServiceDto> getActiveServices();
    ServiceDto getServiceById(Long id);
    List<ServiceDto> searchServices(String query);

    // Admin operations
    List<ServiceDto> getAllServices();
    ServiceDto createService(ServiceCreateRequest request);
    ServiceDto updateService(Long id, ServiceUpdateRequest request);
    ServiceDto toggleServiceStatus(Long id, boolean active);
    void deleteService(Long id);
}
