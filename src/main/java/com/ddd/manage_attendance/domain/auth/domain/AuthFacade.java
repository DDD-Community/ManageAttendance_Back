package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.domain.auth.api.dto.LoginResponse;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthServiceResolver;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthFacade {
    private final OAuthServiceResolver oauthServiceResolver;
    private final UserService userService;

    @Transactional
    public LoginResponse login(
            final OAuthProvider provider, final String token, final String providedName) {
        final OAuthUserInfo oauthUserInfo =
                oauthServiceResolver.resolve(provider).authenticate(token);

        validateOAuthUserInfo(oauthUserInfo);

        final Optional<User> existingUser =
                userService.findByOAuthProviderAndOAuthId(provider, oauthUserInfo.getSub());

        if (existingUser.isPresent()) {
            return LoginResponse.from(existingUser.get(), false);
        }

        return new LoginResponse(null, null, null, null, "회원가입 필요", true);
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
