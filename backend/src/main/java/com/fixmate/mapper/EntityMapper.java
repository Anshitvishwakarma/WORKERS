package com.fixmate.mapper;

import com.fixmate.dto.BookingDto;
import com.fixmate.dto.ProviderServiceDto;
import com.fixmate.dto.ServiceDto;
import com.fixmate.dto.UserSummaryDto;
import com.fixmate.entity.Booking;
import com.fixmate.entity.ProviderService;
import com.fixmate.entity.Service;
import com.fixmate.entity.User;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public UserSummaryDto toUserSummaryDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    public ServiceDto toServiceDto(Service service) {
        if (service == null) {
            return null;
        }
        return new ServiceDto(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getIcon(),
                service.isActive(),
                service.getCreatedAt()
        );
    }

    public ProviderServiceDto toProviderServiceDto(ProviderService ps) {
        if (ps == null) {
            return null;
        }
        User provider = ps.getProvider();
        Service service = ps.getService();

        return new ProviderServiceDto(
                ps.getId(),
                provider != null ? provider.getId() : null,
                provider != null ? provider.getFullName() : null,
                provider != null ? provider.getEmail() : null,
                provider != null ? provider.getPhone() : null,
                provider != null ? provider.getAddress() : null,
                service != null ? service.getId() : null,
                service != null ? service.getName() : null,
                service != null ? service.getDescription() : null,
                service != null ? service.getIcon() : null,
                ps.getPrice(),
                ps.getExperienceYears(),
                ps.getAvailability(),
                ps.getCreatedAt()
        );
    }

    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());

        User customer = booking.getCustomer();
        if (customer != null) {
            dto.setCustomerId(customer.getId());
            dto.setCustomerName(customer.getFullName());
            dto.setCustomerEmail(customer.getEmail());
            dto.setCustomerPhone(customer.getPhone());
        }

        ProviderService ps = booking.getProviderService();
        if (ps != null) {
            dto.setProviderServiceId(ps.getId());
            User provider = ps.getProvider();
            if (provider != null) {
                dto.setProviderId(provider.getId());
                dto.setProviderName(provider.getFullName());
                dto.setProviderPhone(provider.getPhone());
            }
            Service service = ps.getService();
            if (service != null) {
                dto.setServiceId(service.getId());
                dto.setServiceName(service.getName());
                dto.setServiceIcon(service.getIcon());
            }
        }

        dto.setBookingDate(booking.getBookingDate());
        dto.setBookingTime(booking.getBookingTime());
        dto.setServiceAddress(booking.getServiceAddress());
        dto.setDescription(booking.getDescription());
        dto.setPrice(booking.getPrice());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());

        return dto;
    }
}
