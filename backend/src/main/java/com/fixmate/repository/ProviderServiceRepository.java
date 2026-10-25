package com.fixmate.repository;

import com.fixmate.entity.ProviderService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderServiceRepository extends JpaRepository<ProviderService, Long> {

    List<ProviderService> findByProviderId(Long providerId);

    List<ProviderService> findByServiceId(Long serviceId);

    Optional<ProviderService> findByProviderIdAndServiceId(Long providerId, Long serviceId);

    boolean existsByProviderIdAndServiceId(Long providerId, Long serviceId);

    @Query("SELECT ps FROM ProviderService ps " +
           "JOIN FETCH ps.provider p " +
           "JOIN FETCH ps.service s " +
           "WHERE p.active = true AND s.active = true")
    List<ProviderService> findAllActiveWithDetails();

    @Query("SELECT ps FROM ProviderService ps " +
           "JOIN FETCH ps.provider p " +
           "JOIN FETCH ps.service s " +
           "WHERE ps.service.id = :serviceId AND p.active = true AND s.active = true")
    List<ProviderService> findActiveByServiceId(@Param("serviceId") Long serviceId);

    @Query("SELECT ps FROM ProviderService ps " +
           "JOIN FETCH ps.provider p " +
           "JOIN FETCH ps.service s " +
           "WHERE ps.id = :id")
    Optional<ProviderService> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT ps FROM ProviderService ps " +
           "JOIN FETCH ps.service s " +
           "WHERE ps.provider.id = :providerId")
    List<ProviderService> findByProviderIdWithService(@Param("providerId") Long providerId);
}
