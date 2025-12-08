package com.ddd.manage_attendance.domain.oauth.infrastructure.google;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "google.auth")
public class GoogleAuthProperties {
    public static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String DEFAULT_PUBLIC_KEY_URL =
            "https://www.googleapis.com/oauth2/v3/certs";

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String publicKeyUrl = DEFAULT_PUBLIC_KEY_URL;
}
