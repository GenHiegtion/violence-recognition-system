package com.vrs.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;
}