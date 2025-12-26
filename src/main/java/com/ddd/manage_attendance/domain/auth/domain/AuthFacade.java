package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.domain.auth.api.dto.LoginResponse;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthServiceResolver;
import com.ddd.manage_attendance.domain.qr.domain.QrService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthFacade {
    private final OAuthServiceResolver oauthServiceResolver;
    private final UserService userService;
    private final QrService qrService;

    @Transactional
    public LoginResponse login(
            final OAuthProvider provider, final String token, final String providedName) {
        final OAuthUserInfo oauthUserInfo =
                oauthServiceResolver.resolve(provider).authenticate(token);

        validateOAuthUserInfo(oauthUserInfo);

        // 기존 사용자 확인
        final Optional<User> existingUser =
                userService.findByOAuthProviderAndOAuthId(provider, oauthUserInfo.getSub());

        if (existingUser.isPresent()) {
            return LoginResponse.from(existingUser.get(), false);
        }

        // 새 사용자 등록 (Facade가 여러 Service 조율)
        final String userName = determineUserName(oauthUserInfo, providedName);
        final String qrCode = qrService.generateQrCodeKey();

        // TODO: OAuth 로그인 시 generationId와 teamId, jobRole 를 어떻게 처리할지 결정 필요
        final Long defaultGenerationId = 1L;
        final Long defaultTeamId = 1L;
        final JobRole jobRole = JobRole.BACKEND;

        final User newUser =
                userService.registerOAuthUser(
                        provider,
                        oauthUserInfo.getSub(),
                        oauthUserInfo.getEmail(),
                        userName,
                        qrCode,
                        defaultGenerationId,
                        defaultTeamId,
                        jobRole);

        return LoginResponse.from(newUser, true);
    }

    private String determineUserName(final OAuthUserInfo oauthUserInfo, final String providedName) {
        if (providedName != null && !providedName.trim().isEmpty()) {
            return providedName.trim();
        }

        final String oauthName = oauthUserInfo.getName();
        if (oauthName != null && !oauthName.trim().isEmpty()) {
            return oauthName.trim();
        }

        final String email = oauthUserInfo.getEmail();
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }

        return "User";
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
