package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import com.ddd.manage_attendance.domain.auth.domain.User;

public record LoginResponse(
        Long userId,
        String name,
        String email,
        OAuthProvider oauthProvider,
        String message,
        @com.fasterxml.jackson.annotation.JsonProperty("isNewUser") boolean isNewUser,
        String accessToken,
        String refreshToken,
        String oauthRefreshToken) {
    public static LoginResponse from(
            User user,
            String accessToken,
            String refreshToken,
            String oauthRefreshToken,
            boolean isNewUser) {
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getOauthProvider(),
                isNewUser ? "회원가입 완료" : "로그인 성공",
                isNewUser,
                accessToken,
                refreshToken,
                oauthRefreshToken);
    }
}
