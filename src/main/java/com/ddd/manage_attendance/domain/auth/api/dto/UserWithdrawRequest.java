package com.ddd.manage_attendance.domain.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserWithdrawRequest(
        @Schema(
                        description = "OAuth 연결 해제를 위한 토큰 (Google/Apple Access Token)",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                String token) {}
