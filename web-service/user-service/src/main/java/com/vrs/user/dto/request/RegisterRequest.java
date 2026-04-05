package com.vrs.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 4, max = 50, message = "username length must be 4-50")
    private String username;

    @NotBlank(message = "fullName is required")
    @Size(min = 2, max = 150, message = "fullName length must be 2-150")
    private String fullName;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "password length must be 8-100")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "password must contain letters and digits")
    private String password;
}