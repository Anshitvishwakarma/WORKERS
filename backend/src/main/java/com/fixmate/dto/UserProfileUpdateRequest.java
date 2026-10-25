package com.fixmate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @NotBlank(message = "Full name cannot be empty")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Phone number cannot be empty")
    @Size(min = 7, max = 20, message = "Phone must be between 7 and 20 digits")
    private String phone;

    @NotBlank(message = "Address cannot be empty")
    @Size(min = 3, max = 255, message = "Address must be between 3 and 255 characters")
    private String address;

    public UserProfileUpdateRequest() {
    }

    public UserProfileUpdateRequest(String fullName, String phone, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
