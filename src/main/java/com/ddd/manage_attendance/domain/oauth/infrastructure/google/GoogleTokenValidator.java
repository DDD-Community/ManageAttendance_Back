package com.ddd.manage_attendance.domain.oauth.infrastructure.google;

import com.ddd.manage_attendance.domain.oauth.infrastructure.common.AbstractOAuthTokenValidator;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthPublicKeyService;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthTokenParser;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenValidator extends AbstractOAuthTokenValidator<GoogleUserInfo> {
    private static final String PROVIDER = "Google";

    private final GoogleAuthProperties googleAuthProperties;

    public GoogleTokenValidator(
            OAuthTokenParser tokenParser,
            OAuthPublicKeyService publicKeyService,
            GoogleAuthProperties googleAuthProperties) {
        super(tokenParser, publicKeyService);
        this.googleAuthProperties = googleAuthProperties;
    }

    @Override
    protected String getProvider() {
        return PROVIDER;
    }

    @Override
    protected String getIssuer() {
        return GoogleAuthProperties.GOOGLE_ISSUER;
    }

    @Override
    protected String getClientId() {
        return googleAuthProperties.getClientId();
    }

    @Override
    protected String getPublicKeyUrl() {
        return googleAuthProperties.getPublicKeyUrl();
    }

    @Override
    protected Class<GoogleUserInfo> getUserInfoClass() {
        return GoogleUserInfo.class;
    }
}
