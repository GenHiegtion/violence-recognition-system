package com.vrs.user.controller;

import com.vrs.user.dto.request.LoginRequest;
import com.vrs.user.dto.request.RegisterRequest;
import com.vrs.user.dto.response.AuthResponse;
import com.vrs.user.dto.response.UserResponse;
import com.vrs.user.model.User;
import com.vrs.user.service.AuthTokenService;
import com.vrs.user.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthTokenService authTokenService;

    public AuthController(UserService userService, AuthTokenService authTokenService) {
        this.userService = userService;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User account = userService.authenticate(request.getUsername(), request.getPassword())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String token = authTokenService.issueToken(account);
        UserResponse user = userService.toUserResponse(account);
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .issuedAt(LocalDateTime.now())
            .user(user)
            .build();
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        String token = authTokenService.extractBearerToken(authorization);
        boolean revoked = authTokenService.revoke(token);
        return Map.of("loggedOut", revoked);
    }
}
