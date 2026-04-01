package com.vrs.user.controller;

import com.vrs.user.model.AuthResponse;
import com.vrs.user.model.LoginRequest;
import com.vrs.user.model.RegisterRequest;
import com.vrs.user.model.User;
import com.vrs.user.model.UserResponse;
import com.vrs.user.service.AuthTokenService;
import com.vrs.user.service.UserAccountService;
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

    private final UserAccountService userAccountService;
    private final AuthTokenService authTokenService;

    public AuthController(UserAccountService userAccountService, AuthTokenService authTokenService) {
        this.userAccountService = userAccountService;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userAccountService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User account = userAccountService.authenticate(request.username(), request.password())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String token = authTokenService.issueToken(account);
        UserResponse user = userAccountService.toUserResponse(account);
        return new AuthResponse(token, "Bearer", LocalDateTime.now(), user);
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
