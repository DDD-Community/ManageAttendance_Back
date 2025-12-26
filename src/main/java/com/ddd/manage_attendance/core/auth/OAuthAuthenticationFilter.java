package com.ddd.manage_attendance.core.auth;

import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthServiceResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String PROVIDER_HEADER = "X-OAuth-Provider";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";

    private final OAuthServiceResolver oauthServiceResolver;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (shouldSkipAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Authentication> authResult = tryAuthenticate(request);
        authResult.ifPresent(this::storeAuthentication);

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipAuthentication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith(LOGIN_ENDPOINT) || isPublicEndpoint(uri);
    }

    private Optional<Authentication> tryAuthenticate(HttpServletRequest request) {
        return extractOAuthCredentials(request)
                .flatMap(this::verifyAndLoadUser)
                .map(this::createAuthentication);
    }

    private Optional<OAuthCredentials> extractOAuthCredentials(HttpServletRequest request) {
        return extractToken(request)
                .flatMap(
                        token ->
                                extractProvider(request)
                                        .map(provider -> new OAuthCredentials(provider, token)));
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        return Optional.ofNullable(header)
                .filter(h -> h.startsWith(BEARER_PREFIX))
                .map(h -> h.substring(BEARER_PREFIX.length()))
                .filter(token -> !token.trim().isEmpty());
    }

    private Optional<OAuthProvider> extractProvider(HttpServletRequest request) {
        String header = request.getHeader(PROVIDER_HEADER);
        return Optional.ofNullable(header)
                .flatMap(
                        h -> {
                            try {
                                return Optional.of(OAuthProvider.valueOf(h.toUpperCase()));
                            } catch (IllegalArgumentException e) {
                                log.warn("지원하지 않는 OAuth Provider: {}", h);
                                return Optional.empty();
                            }
                        });
    }

    private Optional<User> verifyAndLoadUser(OAuthCredentials credentials) {
        try {
            OAuthUserInfo oauthUserInfo =
                    oauthServiceResolver
                            .resolve(credentials.provider())
                            .authenticate(credentials.token());

            return userService.findByOAuthProviderAndOAuthId(
                    credentials.provider(), oauthUserInfo.getSub());
        } catch (Exception e) {
            log.warn("OAuth 인증 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Authentication createAuthentication(User user) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user.getId(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        auth.setAuthenticated(true);
        return auth;
    }

    private void storeAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("OAuth 인증 성공: userId={}", authentication.getName());
    }

    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/swagger-ui")
                || requestURI.startsWith("/v3/api-docs")
                || requestURI.startsWith("/api-docs")
                || requestURI.startsWith("/h2-console");
    }

    private record OAuthCredentials(OAuthProvider provider, String token) {}
}
