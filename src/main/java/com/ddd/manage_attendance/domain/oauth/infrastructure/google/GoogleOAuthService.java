package com.ddd.manage_attendance.domain.oauth.infrastructure.google;

import com.ddd.manage_attendance.domain.oauth.domain.OAuthService;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.domain.dto.OAuthRevocationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthService implements OAuthService {
    private final GoogleTokenValidator googleTokenValidator;

    @Override
    public OAuthUserInfo authenticate(String idToken) {
        return googleTokenValidator.validate(idToken);
    }

    @Override
    public void revoke(OAuthRevocationRequest request) {
        String token = request.tokenFromClient();
        RestTemplate restTemplate = new RestTemplate();
        String revokeUrl = "https://oauth2.googleapis.com/revoke?token=" + token;
        try {
            restTemplate.postForLocation(revokeUrl, null);
        } catch (Exception e) {
            throw new RuntimeException("Google OAuth 철회 중 오류가 발생했습니다. cause: " + e.getMessage(), e);
        }
    }
}
