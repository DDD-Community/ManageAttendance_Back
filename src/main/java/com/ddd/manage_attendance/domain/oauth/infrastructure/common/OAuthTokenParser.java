package com.ddd.manage_attendance.domain.oauth.infrastructure.common;

import static com.ddd.manage_attendance.core.common.util.Base64Util.decodeBase64UrlToString;

import com.ddd.manage_attendance.domain.oauth.exception.OAuthTokenValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthTokenParser {
    private static final int JWT_MIN_PARTS = 2;
    private static final int HEADER_INDEX = 0;
    private static final int PAYLOAD_INDEX = 1;
    private static final String KID_KEY = "kid";

    private final ObjectMapper objectMapper;

    public String extractKid(String token, String provider) {
        validateToken(token, provider);

        try {
            String[] parts = splitToken(token, provider);
            String headerJson = decodeBase64UrlToString(parts[HEADER_INDEX]);
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
            String kid = (String) header.get(KID_KEY);
            if (kid == null || kid.trim().isEmpty()) {
                throw new OAuthTokenValidationException(provider, "토큰 헤더에 kid가 없습니다.");
            }
            return kid;
        } catch (OAuthTokenValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new OAuthTokenValidationException(provider, "토큰 헤더 파싱에 실패했습니다.", e);
        }
    }

    public <T> T parsePayload(String token, String provider, Class<T> userInfoClass) {
        validateToken(token, provider);

        try {
            String[] parts = splitToken(token, provider);
            String payloadJson = decodeBase64UrlToString(parts[PAYLOAD_INDEX]);
            T userInfo = objectMapper.readValue(payloadJson, userInfoClass);
            if (userInfo == null) {
                throw new OAuthTokenValidationException(provider, "사용자 정보를 파싱할 수 없습니다.");
            }
            return userInfo;
        } catch (OAuthTokenValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new OAuthTokenValidationException(provider, "토큰 페이로드 파싱에 실패했습니다.", e);
        }
    }

    private void validateToken(String token, String provider) {
        if (token == null || token.trim().isEmpty()) {
            throw new OAuthTokenValidationException(provider, "토큰이 비어있습니다.");
        }
        if (token.toLowerCase().startsWith("bearer ")) {
            throw new OAuthTokenValidationException(provider, "토큰은 'Bearer ' 접두사 없이 전송되어야 합니다.");
        }
    }

    private String[] splitToken(String token, String provider) {
        String[] parts = token.split("\\.");
        if (parts.length < JWT_MIN_PARTS) {
            throw new OAuthTokenValidationException(provider, "유효하지 않은 JWT 토큰 형식입니다.");
        }
        return parts;
    }
}
