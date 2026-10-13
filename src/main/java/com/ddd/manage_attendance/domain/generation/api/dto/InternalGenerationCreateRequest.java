package com.ddd.manage_attendance.domain.generation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "내부 기수 생성 요청")
public record InternalGenerationCreateRequest(
        @Schema(description = "기수 이름", example = "5기") @NotBlank @Size(max = 5) String name) {}
