package com.vrs.user.service;

import com.vrs.user.model.RegisterRequest;
import com.vrs.user.model.Role;
import com.vrs.user.model.User;
import com.vrs.user.model.UserResponse;
import com.vrs.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserAccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initData() {
        bootstrapAdmin();
    }

    public UserResponse register(RegisterRequest request) {
        return createAccount(request.username(), request.fullName(), request.password(), Role.USER);
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

    public Optional<User> authenticate(String username, String plainPassword) {
        String normalizedUsername = username.trim().toLowerCase();
        return userRepository.findByUsernameIgnoreCase(normalizedUsername)
                .filter(user -> passwordEncoder.matches(plainPassword, user.getPassword()));
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<UserResponse> findByUsername(String username) {
        User account = userRepository.findByUsernameIgnoreCase(username.trim().toLowerCase()).orElse(null);
        return Optional.ofNullable(account).map(this::toUserResponse);
    }

    public UserResponse toUserResponse(User account) {
        return new UserResponse(
                account.getId(),
                account.getUsername(),
                account.getFullName(),
                account.getRole(),
                account.getCreatedAt()
        );
    }

    private void bootstrapAdmin() {
        if (userRepository.existsByUsernameIgnoreCase("admin")) {
            return;
        }
        createAccount("admin", "System Administrator", "Admin1234", Role.ADMIN);
    }

    private UserResponse toResponse(User account) {
        return toUserResponse(account);
    }
}
