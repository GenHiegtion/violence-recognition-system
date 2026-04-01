package com.vrs.user.config;

import com.vrs.user.model.SessionInfo;
import com.vrs.user.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String AUTH_SESSION_ATTR = "AUTH_SESSION";
    private final AuthTokenService authTokenService;

    public AuthInterceptor(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || path.startsWith("/actuator/")) {
            return true;
        }

        String token = authTokenService.extractBearerToken(request.getHeader("Authorization"));
        if (token == null) {
            return true;
        }

        Optional<SessionInfo> session = authTokenService.resolve(token);
        if (session.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        request.setAttribute(AUTH_SESSION_ATTR, session.get());
        return true;
    }

    public static Optional<SessionInfo> getSession(HttpServletRequest request) {
        Object value = request.getAttribute(AUTH_SESSION_ATTR);
        if (value instanceof SessionInfo sessionInfo) {
            return Optional.of(sessionInfo);
        }
        return Optional.empty();
    }
}
