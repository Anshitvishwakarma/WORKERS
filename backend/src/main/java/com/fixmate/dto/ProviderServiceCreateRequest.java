package com.fixmate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProviderServiceCreateRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "1.0", message = "Price must be at least 1.00")
    private BigDecimal price;

    @Min(value = 0, message = "Experience years must be positive or zero")
    private int experienceYears = 1;

    @NotBlank(message = "Availability hours/days are required")
    private String availability = "Mon-Sat 9AM-6PM";

    public ProviderServiceCreateRequest() {
    }

    public ProviderServiceCreateRequest(Long serviceId, BigDecimal price, int experienceYears, String availability) {
        this.serviceId = serviceId;
        this.price = price;
        this.experienceYears = experienceYears;
        this.availability = availability;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }
}
