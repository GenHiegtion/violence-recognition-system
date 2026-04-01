package com.vrs.user.controller;

import com.vrs.user.config.AuthInterceptor;
import com.vrs.user.model.Role;
import com.vrs.user.model.SessionInfo;
import com.vrs.user.model.UserResponse;
import com.vrs.user.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserAccountService userAccountService;

    public UserController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/me")
    public UserResponse me(HttpServletRequest request) {
        SessionInfo session = requiredSession(request);
        return userAccountService.findByUsername(session.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping
    public List<UserResponse> list(HttpServletRequest request) {
        SessionInfo session = requiredSession(request);
        if (session.role() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return userAccountService.listUsers();
    }

    private SessionInfo requiredSession(HttpServletRequest request) {
        return AuthInterceptor.getSession(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token"));
    }
}
