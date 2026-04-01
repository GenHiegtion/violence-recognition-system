package com.vrs.user.model;

import java.time.LocalDateTime;

public record SessionInfo(
        String token,
        Long userId,
        String username,
        Role role,
        LocalDateTime createdAt
) {
}
