package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "OAuth 제공자는 필수입니다.")
        OAuthProvider provider,

        @NotBlank(message = "인증 토큰은 필수입니다.")
        String token
) {
}
