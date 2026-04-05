package com.vrs.user.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionInfo {

        private String token;
        private Long userId;
        private String username;
        private Role role;
        private LocalDateTime createdAt;
}
