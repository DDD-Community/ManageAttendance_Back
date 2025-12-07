package com.ddd.manage_attendance.domain.oauth.infrastructure.apple;

import com.ddd.manage_attendance.domain.oauth.domain.OAuthService;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppleOAuthService implements OAuthService {
    private final AppleTokenValidator appleTokenValidator;

    @Override
    public OAuthUserInfo authenticate(String identityToken) {
        return appleTokenValidator.validate(identityToken);
    }
}
