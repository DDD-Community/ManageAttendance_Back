package com.ddd.manage_attendance.domain.team.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내부 팀 항목")
public record InternalTeamItem(
        @Schema(description = "팀 ID", example = "1") Long id,
        @Schema(description = "팀 이름", example = "플랫폼팀") String name,
        @Schema(description = "서비스 이름", example = "출석 서비스") String serviceName,
        @Schema(description = "팀 회원 수", example = "5") long memberCount) {}
