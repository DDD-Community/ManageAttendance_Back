package com.ddd.manage_attendance.core.auth;

import com.ddd.manage_attendance.core.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class InternalApiKeyFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(InternalApiKeyFilter.class);
    private static final String HEADER = "X-Internal-Api-Key";
    private final String configuredKey;
    private final ObjectMapper objectMapper;

    public InternalApiKeyFilter(String configuredKey, ObjectMapper objectMapper) {
        this.configuredKey = configuredKey;
        this.objectMapper = objectMapper;
        if (configuredKey == null || configuredKey.isBlank() || configuredKey.length() < 32) {
            log.warn(
                    "Internal API authentication is disabled because its key is not configured securely.");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith("/api/internal/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        boolean configured =
                configuredKey != null && !configuredKey.isBlank() && configuredKey.length() >= 32;
        boolean valid =
                configured
                        && provided != null
                        && MessageDigest.isEqual(
                                configuredKey.getBytes(StandardCharsets.UTF_8),
                                provided.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter()
                    .write(
                            objectMapper.writeValueAsString(
                                    ErrorResponse.of(
                                            "AUTH_INVALID_INTERNAL_KEY",
                                            "유효하지 않은 내부 API 키입니다.",
                                            null)));
            return;
        }
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "internal", null, List.of(new SimpleGrantedAuthority("INTERNAL"))));
        chain.doFilter(request, response);
    }
}
