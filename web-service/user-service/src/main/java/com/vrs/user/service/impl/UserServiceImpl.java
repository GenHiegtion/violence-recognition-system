package com.vrs.user.service.impl;

import com.vrs.user.dto.request.RegisterRequest;
import com.vrs.user.dto.response.UserResponse;
import com.vrs.user.model.Role;
import com.vrs.user.model.User;
import com.vrs.user.repository.UserRepository;
import com.vrs.user.service.UserService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.full-name:System Administrator}")
    private String adminFullName;

    @Value("${app.admin.password:Admin1234}")
    private String adminPassword;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initData() {
        bootstrapAdmin();
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        return createAccount(request.getUsername(), request.getFullName(), request.getPassword(), Role.MEMBER);
    }

    private UserResponse createAccount(String rawUsername, String rawFullName, String plainPassword, Role role) {
        String username = rawUsername.trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User account = new User();
        account.setUsername(username);
        account.setFullName(rawFullName.trim());
        account.setPassword(passwordEncoder.encode(plainPassword));
        account.setRole(role);

        return toResponse(userRepository.save(account));
    }

    @Override
    public Optional<User> authenticate(String username, String plainPassword) {
        String normalizedUsername = username.trim().toLowerCase();
        return userRepository.findByUsernameIgnoreCase(normalizedUsername)
                .filter(user -> passwordEncoder.matches(plainPassword, user.getPassword()));
    }

    @Override
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<UserResponse> findByUsername(String username) {
        User account = userRepository.findByUsernameIgnoreCase(username.trim().toLowerCase()).orElse(null);
        return Optional.ofNullable(account).map(this::toUserResponse);
    }

    @Override
    public UserResponse toUserResponse(User account) {
        return UserResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .fullName(account.getFullName())
                .role(account.getRole())
                .createdAt(account.getCreatedAt())
                .build();
    }

    private void bootstrapAdmin() {
        String normalizedAdminUsername = adminUsername.trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(normalizedAdminUsername)) {
            return;
        }
        createAccount(normalizedAdminUsername, adminFullName, adminPassword, Role.ADMIN);
    }

    private UserResponse toResponse(User account) {
        return toUserResponse(account);
    }
}