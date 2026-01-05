package com.ddd.manage_attendance.domain.oauth.domain;

import com.ddd.manage_attendance.domain.oauth.domain.dto.OAuthRevocationRequest;

public interface OAuthService {
    OAuthUserInfo authenticate(String token);

    void revoke(OAuthRevocationRequest request);
}
