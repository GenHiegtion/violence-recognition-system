package com.vrs.user.model;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        Role role,
        LocalDateTime createdAt
) {
}
