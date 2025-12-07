package com.ddd.manage_attendance.domain.auth.application;

import com.ddd.manage_attendance.domain.auth.api.dto.LoginResponse;
import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.oauth.application.OAuthAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final OAuthAuthenticationService oauthAuthenticationService;

    public LoginResponse login(final OAuthProvider provider, final String token) {
        final User user = oauthAuthenticationService.authenticateWithOAuth(provider, token);
        final boolean isNewUser = oauthAuthenticationService.isNewUser(provider, user.getOauthId());

        return LoginResponse.from(user, isNewUser);
    }
}
