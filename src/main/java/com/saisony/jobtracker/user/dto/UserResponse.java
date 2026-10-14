package com.saisony.jobtracker.user.dto;

import java.time.OffsetDateTime;

public class UserResponse {

    private Long id;
    private String email;
    private String role;
    private OffsetDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, String role, OffsetDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
