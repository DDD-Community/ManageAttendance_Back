package com.ddd.manage_attendance.domain.attendance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기수 정보")
public record InternalGenerationInfo(
        @Schema(description = "기수 ID", example = "1") Long id,
        @Schema(description = "기수 이름", example = "5기") String name) {}
