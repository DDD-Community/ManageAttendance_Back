package com.ddd.manage_attendance.domain.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[유저] 회원 탈퇴 요청 DTO")
public record UserWithdrawRequest(
        @Schema(
                        description = "OAuth 연결 해제를 위한 토큰 (Google/Apple Access Token)",
                        example = "ya29.a0AfH6SMBx...",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                String token) {}
