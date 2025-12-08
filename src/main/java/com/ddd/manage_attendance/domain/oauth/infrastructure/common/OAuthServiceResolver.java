package com.ddd.manage_attendance.domain.oauth.infrastructure.common;

import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthServiceResolver {
    private final Map<String, OAuthService> oauthServices;

    public OAuthService resolve(final OAuthProvider provider) {
        final String beanName = provider.name().toLowerCase() + "OAuthService";
        final OAuthService service = oauthServices.get(beanName);
        if (service == null) {
            throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + provider);
        }
        return service;
    }
}
