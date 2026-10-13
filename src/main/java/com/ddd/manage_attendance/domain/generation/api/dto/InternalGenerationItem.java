package com.ddd.manage_attendance.domain.generation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내부 기수 항목")
public record InternalGenerationItem(
        @Schema(description = "기수 ID", example = "1") Long id,
        @Schema(description = "기수 이름", example = "5기") String name,
        @Schema(description = "회원 수", example = "10") long memberCount) {}
