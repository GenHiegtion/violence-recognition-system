package com.vrs.user.model;

import java.time.LocalDateTime;

public record AuthResponse(
        String token,
        String tokenType,
        LocalDateTime issuedAt,
        UserResponse user
) {
}
