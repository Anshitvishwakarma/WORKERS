package com.fixmate.service.impl;

import com.fixmate.dto.ProviderServiceCreateRequest;
import com.fixmate.dto.ProviderServiceDto;
import com.fixmate.dto.ProviderServiceUpdateRequest;
import com.fixmate.entity.ProviderService;
import com.fixmate.entity.User;
import com.fixmate.enums.Role;
import com.fixmate.exception.BadRequestException;
import com.fixmate.exception.ConflictException;
import com.fixmate.exception.ForbiddenException;
import com.fixmate.exception.ResourceNotFoundException;
import com.fixmate.mapper.EntityMapper;
import com.fixmate.repository.BookingRepository;
import com.fixmate.repository.ProviderServiceRepository;
import com.fixmate.repository.ServiceRepository;
import com.fixmate.repository.UserRepository;
import com.fixmate.service.ProviderServiceService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
public class ProviderServiceServiceImpl implements ProviderServiceService {

    private final ProviderServiceRepository providerServiceRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final EntityMapper entityMapper;

    public ProviderServiceServiceImpl(ProviderServiceRepository providerServiceRepository,
                                      UserRepository userRepository,
                                      ServiceRepository serviceRepository,
                                      BookingRepository bookingRepository,
                                      EntityMapper entityMapper) {
        this.providerServiceRepository = providerServiceRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.bookingRepository = bookingRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderServiceDto> getAllActiveProviders() {
        return providerServiceRepository.findAllActiveWithDetails()
                .stream()
                .map(entityMapper::toProviderServiceDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderServiceDto> getProvidersByServiceId(Long serviceId) {
        return providerServiceRepository.findActiveByServiceId(serviceId)
                .stream()
                .map(entityMapper::toProviderServiceDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderServiceDto getProviderServiceById(Long id) {
        ProviderService ps = providerServiceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider Service", "id", id));
        return entityMapper.toProviderServiceDto(ps);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderServiceDto> getMyServices(String providerEmail) {
        User provider = getProviderUser(providerEmail);
        return providerServiceRepository.findByProviderIdWithService(provider.getId())
                .stream()
                .map(entityMapper::toProviderServiceDto)
                .toList();
    }

    @Override
    @Transactional
    public ProviderServiceDto addServiceOffering(String providerEmail, ProviderServiceCreateRequest request) {
        User provider = getProviderUser(providerEmail);

        if (!provider.isActive()) {
            throw new BadRequestException("Your provider account is currently inactive.");
        }

        com.fixmate.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        if (!service.isActive()) {
            throw new BadRequestException("This service is currently disabled in the platform catalog.");
        }

        if (providerServiceRepository.existsByProviderIdAndServiceId(provider.getId(), service.getId())) {
            throw new ConflictException("You already offer this service in your catalog.");
        }

        ProviderService ps = new ProviderService();
        ps.setProvider(provider);
        ps.setService(service);
        ps.setPrice(request.getPrice());
        ps.setExperienceYears(request.getExperienceYears());
        ps.setAvailability(request.getAvailability().trim());

        ProviderService saved = providerServiceRepository.save(ps);
        return entityMapper.toProviderServiceDto(saved);
    }

    @Override
    @Transactional
    public ProviderServiceDto updateServiceOffering(String providerEmail, Long providerServiceId, ProviderServiceUpdateRequest request) {
        User provider = getProviderUser(providerEmail);

        ProviderService ps = providerServiceRepository.findByIdWithDetails(providerServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider Service", "id", providerServiceId));

        // Ownership verification (IDOR prevention)
        if (!ps.getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("You cannot modify service offerings belonging to another provider.");
        }

        ps.setPrice(request.getPrice());
        ps.setExperienceYears(request.getExperienceYears());
        ps.setAvailability(request.getAvailability().trim());

        ProviderService updated = providerServiceRepository.save(ps);
        return entityMapper.toProviderServiceDto(updated);
    }

    @Override
    @Transactional
    public void removeServiceOffering(String providerEmail, Long providerServiceId) {
        User provider = getProviderUser(providerEmail);

        ProviderService ps = providerServiceRepository.findByIdWithDetails(providerServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider Service", "id", providerServiceId));

        // Ownership verification (IDOR prevention)
        if (!ps.getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("You cannot delete service offerings belonging to another provider.");
        }

        if (bookingRepository.existsByProviderServiceId(providerServiceId)) {
            throw new BadRequestException("Cannot delete this service offering because bookings are associated with it. You can adjust your price or availability instead.");
        }

        providerServiceRepository.delete(ps);
    }

    private User getProviderUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (user.getRole() != Role.PROVIDER) {
            throw new ForbiddenException("Only registered service providers can perform this operation.");
        }
        return user;
    }
}
