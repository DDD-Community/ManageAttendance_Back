package com.ddd.manage_attendance.domain.oauth.infrastructure.apple;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "apple.auth")
public class AppleAuthProperties {
    public static final String APPLE_ISSUER = "https://appleid.apple.com";

    private String clientId;
    private String redirectUri;
    private String publicKeyUrl = "https://appleid.apple.com/auth/keys";
}
