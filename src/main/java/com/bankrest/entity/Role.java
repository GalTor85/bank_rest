package com.bankrest.entity;

public enum Role {
    ADMIN,
    USER;


    private static final String ROLE_PREFIX = "ROLE_";

    public String getAuthority() {
        return ROLE_PREFIX + this.name();
    }
}