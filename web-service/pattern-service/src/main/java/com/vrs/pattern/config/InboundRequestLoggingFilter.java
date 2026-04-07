package com.vrs.pattern.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class InboundRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.info("[INBOUND] pattern-service received {} {}", method, uri);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info(
                    "[INBOUND] pattern-service completed {} {} status={} durationMs={}",
                    method,
                    uri,
                    response.getStatus(),
                    duration
            );
        }
    }
}
