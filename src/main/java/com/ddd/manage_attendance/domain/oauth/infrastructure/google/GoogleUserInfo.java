package com.ddd.manage_attendance.domain.oauth.infrastructure.google;

import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleUserInfo implements OAuthUserInfo {
    private String sub;
    private String email;
    private String iss;
    private String aud;
    private Long exp;
    private String name;
}
