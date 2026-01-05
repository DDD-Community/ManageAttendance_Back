package com.ddd.manage_attendance.domain.oauth.infrastructure.apple;

import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleUserInfo implements OAuthUserInfo {
    private String sub;

    @JsonProperty("email")
    private String email;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String iss;
    private String aud;
    private Long exp;
    private String name;
}
