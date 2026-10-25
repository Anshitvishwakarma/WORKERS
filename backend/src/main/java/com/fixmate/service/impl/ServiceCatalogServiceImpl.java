package com.fixmate.service.impl;

import com.fixmate.dto.ServiceCreateRequest;
import com.fixmate.dto.ServiceDto;
import com.fixmate.dto.ServiceUpdateRequest;
import com.fixmate.exception.ConflictException;
import com.fixmate.exception.ResourceNotFoundException;
import com.fixmate.mapper.EntityMapper;
import com.fixmate.repository.ServiceRepository;
import com.fixmate.service.ServiceCatalogService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceCatalogServiceImpl implements ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final EntityMapper entityMapper;

    public ServiceCatalogServiceImpl(ServiceRepository serviceRepository, EntityMapper entityMapper) {
        this.serviceRepository = serviceRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDto> getActiveServices() {
        return serviceRepository.findByActiveTrue()
                .stream()
                .map(entityMapper::toServiceDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDto getServiceById(Long id) {
        com.fixmate.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
        return entityMapper.toServiceDto(service);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDto> searchServices(String query) {
        if (query == null || query.isBlank()) {
            return getActiveServices();
        }
        return serviceRepository.searchActiveServices(query.trim())
                .stream()
                .map(entityMapper::toServiceDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDto> getAllServices() {
        return serviceRepository.findAll()
                .stream()
                .map(entityMapper::toServiceDto)
                .toList();
    }

    @Override
    @Transactional
    public ServiceDto createService(ServiceCreateRequest request) {
        String name = request.getName().trim();
        if (serviceRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("A service named '" + name + "' already exists.");
        }

        com.fixmate.entity.Service service = new com.fixmate.entity.Service();
        service.setName(name);
        service.setDescription(request.getDescription().trim());
        service.setIcon(request.getIcon() != null ? request.getIcon().trim() : "bi-tools");
        service.setActive(true);

        com.fixmate.entity.Service saved = serviceRepository.save(service);
        return entityMapper.toServiceDto(saved);
    }

    @Override
    @Transactional
    public ServiceDto updateService(Long id, ServiceUpdateRequest request) {
        com.fixmate.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));

        String newName = request.getName().trim();
        if (!service.getName().equalsIgnoreCase(newName) && serviceRepository.existsByNameIgnoreCase(newName)) {
            throw new ConflictException("Another service with name '" + newName + "' already exists.");
        }

        service.setName(newName);
        service.setDescription(request.getDescription().trim());
        if (request.getIcon() != null) {
            service.setIcon(request.getIcon().trim());
        }
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }

        com.fixmate.entity.Service updated = serviceRepository.save(service);
        return entityMapper.toServiceDto(updated);
    }

    @Override
    @Transactional
    public ServiceDto toggleServiceStatus(Long id, boolean active) {
        com.fixmate.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));

        service.setActive(active);
        com.fixmate.entity.Service updated = serviceRepository.save(service);
        return entityMapper.toServiceDto(updated);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        com.fixmate.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));

        // Soft delete / deactivate
        service.setActive(false);
        serviceRepository.save(service);
    }
}
