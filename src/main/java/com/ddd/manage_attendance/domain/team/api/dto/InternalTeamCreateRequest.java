package com.ddd.manage_attendance.domain.team.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "내부 팀 생성 요청")
public record InternalTeamCreateRequest(
        @Schema(description = "팀 이름", example = "플랫폼팀") @NotBlank @Size(max = 30) String name,
        @Schema(description = "서비스 이름", example = "출석 서비스") @Size(max = 100) String serviceName) {}
