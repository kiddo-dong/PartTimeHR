package com.example.PartTimeHR.auth.domain;

import com.example.PartTimeHR.employer.domain.Role;

public interface AuthPrincipal {
    Long getId();
    String getEmail();
    String getPassword();
    Role getRole();

    default boolean isEmailVerified() {
        return true; // 기본값
    }
}