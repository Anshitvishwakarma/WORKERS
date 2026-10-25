package com.fixmate.service;

import com.fixmate.dto.ProviderServiceCreateRequest;
import com.fixmate.dto.ProviderServiceDto;
import com.fixmate.dto.ProviderServiceUpdateRequest;

import java.util.List;

public interface ProviderServiceService {
    List<ProviderServiceDto> getAllActiveProviders();
    List<ProviderServiceDto> getProvidersByServiceId(Long serviceId);
    ProviderServiceDto getProviderServiceById(Long id);

    // Provider logged-in operations
    List<ProviderServiceDto> getMyServices(String providerEmail);
    ProviderServiceDto addServiceOffering(String providerEmail, ProviderServiceCreateRequest request);
    ProviderServiceDto updateServiceOffering(String providerEmail, Long providerServiceId, ProviderServiceUpdateRequest request);
    void removeServiceOffering(String providerEmail, Long providerServiceId);
}
