package com.ddd.manage_attendance.domain.oauth.infrastructure.google;

import com.ddd.manage_attendance.domain.oauth.domain.OAuthService;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService implements OAuthService {
    private final GoogleTokenValidator googleTokenValidator;

    @Override
    public OAuthUserInfo authenticate(String idToken) {
        return googleTokenValidator.validate(idToken);
    }
}
