package com.ddd.manage_attendance.domain.schedule.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record InternalScheduleSyncItem(
        @NotNull(message = "일정 날짜는 필수입니다.") @Schema(description = "일정 날짜", example = "2026-11-07")
                LocalDate date,
        @NotNull(message = "일정 시간은 필수입니다.") @Schema(description = "일정 시간", example = "14:00")
                LocalTime scheduleTime,
        @NotBlank(message = "일정 이름은 필수입니다.")
                @Size(max = 30, message = "일정 이름은 30자 이하여야 합니다.")
                @Schema(description = "일정 이름", example = "오리엔테이션")
                String name,
        @Size(max = 100, message = "일정 설명은 100자 이하여야 합니다.")
                @Schema(description = "일정 설명", example = "오리엔테이션")
                String description) {}
