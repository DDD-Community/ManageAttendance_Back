package com.ddd.manage_attendance.domain.oauth.domain;

public interface OAuthService {
    OAuthUserInfo authenticate(String token);

    void revoke(String token);
}
