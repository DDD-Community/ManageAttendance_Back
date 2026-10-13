package com.ddd.manage_attendance.domain.attendance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "출석 보드 회원 항목")
public record InternalMemberItem(
        @Schema(description = "회원 ID", example = "1") Long id,
        @Schema(description = "회원 이름", example = "홍길동") String name,
        @Schema(description = "소속 팀 ID", example = "2") Long teamId,
        @Schema(description = "소속 팀 이름", example = "플랫폼팀") String teamName,
        @Schema(description = "직무", example = "BACKEND") String job) {}
