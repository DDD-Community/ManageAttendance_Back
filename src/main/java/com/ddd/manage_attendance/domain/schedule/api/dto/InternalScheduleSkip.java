package com.ddd.manage_attendance.domain.schedule.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record InternalScheduleSkip(
        @Schema(description = "일정 날짜", example = "2026-11-07") LocalDate date,
        @Schema(description = "처리 사유", example = "HAS_ATTENDANCE") String reason) {}
