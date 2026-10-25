package com.fixmate.enums;

public enum Role {
    CUSTOMER,
    PROVIDER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
