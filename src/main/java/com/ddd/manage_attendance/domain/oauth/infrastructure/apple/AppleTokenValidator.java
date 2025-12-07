package com.ddd.manage_attendance.domain.oauth.infrastructure.apple;

import com.ddd.manage_attendance.domain.oauth.infrastructure.common.AbstractOAuthTokenValidator;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthPublicKeyService;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthTokenParser;
import org.springframework.stereotype.Component;

@Component
public class AppleTokenValidator extends AbstractOAuthTokenValidator<AppleUserInfo> {
    private static final String PROVIDER = "Apple";

    private final AppleAuthProperties appleAuthProperties;

    public AppleTokenValidator(
            OAuthTokenParser tokenParser,
            OAuthPublicKeyService publicKeyService,
            AppleAuthProperties appleAuthProperties) {
        super(tokenParser, publicKeyService);
        this.appleAuthProperties = appleAuthProperties;
    }

    @Override
    protected String getProvider() {
        return PROVIDER;
    }

    @Override
    protected String getIssuer() {
        return AppleAuthProperties.APPLE_ISSUER;
    }

    @Override
    protected String getClientId() {
        return appleAuthProperties.getClientId();
    }

    @Override
    protected String getPublicKeyUrl() {
        return appleAuthProperties.getPublicKeyUrl();
    }

    @Override
    protected Class<AppleUserInfo> getUserInfoClass() {
        return AppleUserInfo.class;
    }
}
