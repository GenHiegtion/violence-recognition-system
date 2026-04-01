package com.vrs.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username is required")
        @Size(min = 4, max = 50, message = "username length must be 4-50")
        String username,
        @NotBlank(message = "fullName is required")
        @Size(min = 2, max = 150, message = "fullName length must be 2-150")
        String fullName,
        @NotBlank(message = "password is required")
        @Size(min = 8, max = 100, message = "password length must be 8-100")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "password must contain letters and digits")
        String password
) {
}
