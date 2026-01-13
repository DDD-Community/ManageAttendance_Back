package com.ddd.manage_attendance.domain.oauth.infrastructure.google;

import com.ddd.manage_attendance.domain.oauth.domain.OAuthService;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.domain.dto.OAuthRevocationRequest;
import com.ddd.manage_attendance.domain.oauth.exception.OAuthTokenValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthService implements OAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @Override
    public OAuthUserInfo authenticate(String idToken) {
        try {
            GoogleUserInfo userInfo = restTemplate.getForObject(TOKEN_INFO_URL + idToken, GoogleUserInfo.class);

            if (userInfo == null) {
                throw new OAuthTokenValidationException("Google", "구글 API 응답이 비어있습니다.");
            }

            return userInfo;

        } catch (HttpClientErrorException e) {
            throw new OAuthTokenValidationException("Google", "유효하지 않은 구글 ID Token입니다.", e);
        } catch (Exception e) {
            throw new OAuthTokenValidationException("Google", "구글 토큰 검증 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void revoke(OAuthRevocationRequest request) {
        String token = request.tokenFromClient();
        String revokeUrl = "https://oauth2.googleapis.com/revoke?token=" + token;
        try {
            restTemplate.postForLocation(revokeUrl, null);
        } catch (Exception e) {
            throw new RuntimeException("Google OAuth 철회 중 오류가 발생했습니다. cause: " + e.getMessage(), e);
        }
    }
}
