package com.ddd.manage_attendance.domain.oauth.infrastructure.common;

import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.exception.OAuthTokenValidationException;
import io.jsonwebtoken.Jwts;
import java.security.PublicKey;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractOAuthTokenValidator<T extends OAuthUserInfo> {
    protected final OAuthTokenParser tokenParser;
    protected final OAuthPublicKeyService publicKeyService;

    protected abstract String getProvider();

    protected abstract String getIssuer();

    protected abstract String getClientId();

    protected abstract String getPublicKeyUrl();

    protected abstract Class<T> getUserInfoClass();

    public T validate(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new OAuthTokenValidationException(getProvider(), "토큰이 비어있습니다.");
        }

        String kid = tokenParser.extractKid(token, getProvider());
        if (kid == null || kid.trim().isEmpty()) {
            throw new OAuthTokenValidationException(getProvider(), "토큰에서 kid를 추출할 수 없습니다.");
        }

        T userInfo = tokenParser.parsePayload(token, getProvider(), getUserInfoClass());
        if (userInfo == null) {
            throw new OAuthTokenValidationException(getProvider(), "사용자 정보를 파싱할 수 없습니다.");
        }

        PublicKey publicKey = publicKeyService.getPublicKey(kid, getPublicKeyUrl(), getProvider());
        verifySignature(token, publicKey);
        validateClaims(userInfo);
        return userInfo;
    }

    private void verifySignature(String token, PublicKey publicKey) {
        try {
            Jwts.parser()
                    .clockSkewSeconds(60 * 60 * 24) // 24시간 허용 (서버 시간 오차 문제 해결)
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (Exception e) {
            throw new OAuthTokenValidationException(getProvider(), "토큰 서명 검증에 실패했습니다.", e);
        }
    }

    private void validateClaims(T userInfo) {
        validateIssuer(userInfo);
        validateAudience(userInfo);
    }

    private void validateIssuer(T userInfo) {
        if (!getIssuer().equals(userInfo.getIss())) {
            throw new OAuthTokenValidationException(getProvider(), "유효하지 않은 토큰 발급자입니다.");
        }
    }

    private void validateAudience(T userInfo) {
        String clientId = getClientId();
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new OAuthTokenValidationException(getProvider(), "Client ID가 설정되지 않았습니다.");
        }
        if (userInfo.getAud() == null || !clientId.equals(userInfo.getAud())) {
            throw new OAuthTokenValidationException(getProvider(), "유효하지 않은 클라이언트 ID입니다.");
        }
    }
}
