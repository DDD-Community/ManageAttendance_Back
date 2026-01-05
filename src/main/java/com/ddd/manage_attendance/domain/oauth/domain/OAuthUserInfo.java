package com.ddd.manage_attendance.domain.oauth.domain;

public interface OAuthUserInfo {
    String getSub();

    String getEmail();

    String getName();

    String getIss();

    String getAud();

    Long getExp();

    default String getRefreshToken() {
        return null;
    }
}
