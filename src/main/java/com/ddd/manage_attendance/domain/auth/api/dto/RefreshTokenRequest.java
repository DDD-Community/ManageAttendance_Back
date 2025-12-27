package com.ddd.manage_attendance.domain.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(title = "[인증] 토큰 재발급 요청 DTO")
public record RefreshTokenRequest(
        @Schema(description = "Refresh Token", example = "eyJ...")
                @NotBlank(message = "Refresh Token은 필수입니다.")
                String refreshToken) {}
