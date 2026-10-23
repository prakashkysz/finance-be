package com.finance.finance.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.finance.entity.User;
import com.finance.finance.enums.Role;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        Long id,
        String email,
        String mobileNumber,
        Role role,
        Instant createdAt,
        Instant lastLoginAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getMobileNumber(), user.getRole(),
                user.getCreatedAt(), user.getLastLoginAt());
    }
}
