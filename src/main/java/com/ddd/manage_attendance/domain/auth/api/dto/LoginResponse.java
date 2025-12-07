package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import com.ddd.manage_attendance.domain.auth.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private final Long userId;
    private final String name;
    private final String email;
    private final OAuthProvider oauthProvider;
    private final String message;
    private final boolean isNewUser;

    public static LoginResponse from(User user, boolean isNewUser) {
        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .oauthProvider(user.getOauthProvider())
                .message(isNewUser ? "회원가입 완료" : "로그인 성공")
                .isNewUser(isNewUser)
                .build();
    }
}
