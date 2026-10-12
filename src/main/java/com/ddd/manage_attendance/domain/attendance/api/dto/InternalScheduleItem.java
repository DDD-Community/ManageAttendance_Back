package com.ddd.manage_attendance.domain.attendance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "출석 보드 일정 항목")
public record InternalScheduleItem(
        @Schema(description = "일정 ID", example = "1") Long id,
        @Schema(description = "일정 이름", example = "정기 세션") String name,
        @Schema(description = "일정 날짜", example = "2026-10-12") LocalDate date,
        @Schema(description = "일정 시간", example = "19:00:00") LocalTime scheduleTime) {}
