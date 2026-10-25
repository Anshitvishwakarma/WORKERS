package com.fixmate.repository;

import com.fixmate.entity.Booking;
import com.fixmate.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.customer c " +
           "JOIN FETCH b.providerService ps " +
           "JOIN FETCH ps.provider p " +
           "JOIN FETCH ps.service s " +
           "WHERE b.customer.id = :customerId " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerIdWithDetails(@Param("customerId") Long customerId);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.customer c " +
           "JOIN FETCH b.providerService ps " +
           "JOIN FETCH ps.provider p " +
           "JOIN FETCH ps.service s " +
           "WHERE ps.provider.id = :providerId " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findByProviderIdWithDetails(@Param("providerId") Long providerId);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.customer c " +
           "JOIN FETCH b.providerService ps " +
           "JOIN FETCH ps.provider p " +
           "JOIN FETCH ps.service s " +
           "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.customer c " +
           "JOIN FETCH b.providerService ps " +
           "JOIN FETCH ps.provider p " +
           "JOIN FETCH ps.service s " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findAllWithDetails();

    long countByStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.price), 0) FROM Booking b WHERE b.status = 'COMPLETED'")
    BigDecimal calculateTotalCompletedRevenue();

    boolean existsByProviderServiceId(Long providerServiceId);
}
