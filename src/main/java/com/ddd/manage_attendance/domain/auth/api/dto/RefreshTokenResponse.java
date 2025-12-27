package com.ddd.manage_attendance.domain.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[인증] 토큰 재발급 응답 DTO")
public record RefreshTokenResponse(
        @Schema(description = "새로운 Access Token") String accessToken,
        @Schema(description = "새로운 Refresh Token") String refreshToken) {

    public static RefreshTokenResponse from(String accessToken, String refreshToken) {
        return new RefreshTokenResponse(accessToken, refreshToken);
    }
}
