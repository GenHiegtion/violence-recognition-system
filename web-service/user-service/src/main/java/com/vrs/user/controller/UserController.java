package com.vrs.user.controller;

import com.vrs.user.config.AuthInterceptor;
import com.vrs.user.dto.response.UserResponse;
import com.vrs.user.model.Role;
import com.vrs.user.model.SessionInfo;
import com.vrs.user.service.UserService;
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

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(HttpServletRequest request) {
        SessionInfo session = requiredSession(request);
        return userService.findByUsername(session.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping
    public List<UserResponse> list(HttpServletRequest request) {
        SessionInfo session = requiredSession(request);
        if (session.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return userService.listUsers();
    }

    private SessionInfo requiredSession(HttpServletRequest request) {
        return AuthInterceptor.getSession(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token"));
    }
}
