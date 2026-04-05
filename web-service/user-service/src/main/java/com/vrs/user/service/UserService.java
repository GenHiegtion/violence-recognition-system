package com.vrs.user.service;

import com.vrs.user.dto.request.RegisterRequest;
import com.vrs.user.dto.response.UserResponse;
import com.vrs.user.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    UserResponse register(RegisterRequest request);

    Optional<User> authenticate(String username, String plainPassword);

    List<UserResponse> listUsers();

    Optional<UserResponse> findByUsername(String username);

    UserResponse toUserResponse(User account);
}