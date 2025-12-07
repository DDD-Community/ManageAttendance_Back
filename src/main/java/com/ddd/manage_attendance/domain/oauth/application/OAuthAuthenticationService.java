package com.ddd.manage_attendance.domain.oauth.application;

import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthServiceResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthAuthenticationService {
    private final OAuthServiceResolver oauthServiceResolver;
    private final UserService userService;

    @Transactional
    public User authenticateWithOAuth(final OAuthProvider provider, final String token) {
        validateToken(token);

        final OAuthUserInfo oauthUserInfo = authenticateToken(provider, token);
        validateOAuthUserInfo(oauthUserInfo);

        final String sub = oauthUserInfo.getSub();
        final String email = oauthUserInfo.getEmail();
        final String name = oauthUserInfo.getName();

        return userService.loginOrRegisterOAuthUser(provider, sub, email, name);
    }

    public boolean isNewUser(final OAuthProvider provider, final String oauthId) {
        return !userService.existsByOauthProviderAndOauthId(provider, oauthId);
    }

    private void validateToken(final String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("인증 토큰이 비어있습니다.");
        }
    }

    private OAuthUserInfo authenticateToken(final OAuthProvider provider, final String token) {
        return oauthServiceResolver.resolve(provider).authenticate(token);
    }

    private void validateOAuthUserInfo(final OAuthUserInfo oauthUserInfo) {
        if (oauthUserInfo == null) {
            throw new IllegalStateException("OAuth 사용자 정보를 가져올 수 없습니다.");
        }

        final String sub = oauthUserInfo.getSub();
        if (sub == null || sub.trim().isEmpty()) {
            throw new IllegalStateException("OAuth 사용자 ID(sub)가 없습니다.");
        }
    }
}
