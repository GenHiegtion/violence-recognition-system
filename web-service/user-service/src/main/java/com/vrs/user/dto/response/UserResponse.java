package com.vrs.user.dto.response;

import com.vrs.user.model.Role;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private LocalDateTime createdAt;
}