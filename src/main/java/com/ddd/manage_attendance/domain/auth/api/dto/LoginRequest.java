package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(title = "[인증] 로그인 요청 DTO")
public record LoginRequest(
        @Schema(
                        description = "OAuth 제공자",
                        example = "GOOGLE",
                        allowableValues = {"GOOGLE", "APPLE"})
                @NotNull(message = "OAuth 제공자는 필수입니다.")
                OAuthProvider provider,
        @Schema(description = "OAuth Access Token", example = "ya29.a0AfH6SMBx...")
                @NotBlank(message = "인증 토큰은 필수입니다.")
                String token) {}
