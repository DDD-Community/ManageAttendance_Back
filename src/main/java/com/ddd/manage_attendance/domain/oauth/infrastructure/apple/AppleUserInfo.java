package com.ddd.manage_attendance.domain.oauth.infrastructure.apple;

import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleUserInfo implements OAuthUserInfo {
    private String sub;
    private String email;
    private String iss;
    private String aud;
    private Long exp;
    private String name;
}
