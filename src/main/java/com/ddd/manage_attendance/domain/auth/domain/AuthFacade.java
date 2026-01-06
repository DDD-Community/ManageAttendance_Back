package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.domain.auth.api.dto.LoginResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.RefreshTokenResponse;
import com.ddd.manage_attendance.domain.auth.infrastructure.jwt.TokenProvider;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthServiceResolver;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthFacade {
    private final OAuthServiceResolver oauthServiceResolver;
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    @Transactional
    public LoginResponse login(
            final OAuthProvider provider, final String token, final String providedName) {
        final OAuthUserInfo oauthUserInfo =
                oauthServiceResolver.resolve(provider).authenticate(token);

        validateOAuthUserInfo(oauthUserInfo);

        final Optional<User> existingUser =
                userService.findByOAuthProviderAndOAuthId(provider, oauthUserInfo.getSub());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            String oauthRefreshToken = null;
            if (oauthUserInfo.getRefreshToken() != null) {
                user.updateOAuthRefreshToken(oauthUserInfo.getRefreshToken());
                oauthRefreshToken = oauthUserInfo.getRefreshToken();
            }

            String accessToken = tokenProvider.createAccessToken(user.getId());
            String refreshToken = tokenProvider.createRefreshToken(user.getId());

            saveRefreshToken(user.getId(), refreshToken);

            return LoginResponse.from(user, accessToken, refreshToken, oauthRefreshToken, false);
        }

        return new LoginResponse(
                null,
                null,
                null,
                null,
                "회원가입 필요",
                true,
                null,
                null,
                oauthUserInfo.getRefreshToken());
    }

    @Transactional
    public RefreshTokenResponse refresh(final String refreshToken) {
        RefreshToken storedToken =
                refreshTokenRepository
                        .findByToken(refreshToken)
                        .orElseThrow(InvalidTokenException::new);

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new ExpiredTokenException();
        }

        if (!tokenProvider.validateToken(refreshToken)) {
            refreshTokenRepository.delete(storedToken);
            throw new InvalidTokenException();
        }

        refreshTokenRepository.delete(storedToken);

        final Long userId = storedToken.getUserId();
        final User user = userService.getUser(userId);

        final String newAccessToken = tokenProvider.createAccessToken(user.getId());
        final String newRefreshToken = tokenProvider.createRefreshToken(user.getId());

        saveRefreshToken(userId, newRefreshToken);

        return RefreshTokenResponse.from(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(final Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private void saveRefreshToken(final Long userId, final String refreshToken) {
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken newRefreshToken =
                RefreshToken.createToken(
                        userId,
                        refreshToken,
                        LocalDateTime.now().plusSeconds(refreshTokenValidityInSeconds));

        refreshTokenRepository.save(newRefreshToken);
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
