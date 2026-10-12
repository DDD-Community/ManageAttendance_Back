package com.ddd.manage_attendance.domain.generation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InternalGenerationCreateRequest(@NotBlank @Size(max = 5) String name) {}
