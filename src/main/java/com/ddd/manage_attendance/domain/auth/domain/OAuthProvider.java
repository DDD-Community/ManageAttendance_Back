package com.ddd.manage_attendance.domain.auth.domain;

public enum OAuthProvider {
    APPLE("애플"),
    GOOGLE("구글"),
    KAKAO("카카오"),
    NONE("없음");

    private final String description;

    OAuthProvider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
