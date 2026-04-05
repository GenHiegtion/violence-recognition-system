package com.vrs.user.service;

import com.vrs.user.model.SessionInfo;
import com.vrs.user.model.User;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public String issueToken(User account) {
        String token = UUID.randomUUID().toString();
        SessionInfo session = SessionInfo.builder()
            .token(token)
            .userId(account.getId())
            .username(account.getUsername())
            .role(account.getRole())
            .createdAt(LocalDateTime.now())
            .build();
        sessions.put(token, session);
        return token;
    }

    public Optional<SessionInfo> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(token));
    }

    public boolean revoke(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return sessions.remove(token) != null;
    }

    public String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }
        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) {
            return null;
        }
        return authorizationHeader.substring(prefix.length()).trim();
    }
}
