package com.ddd.manage_attendance.domain.schedule.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

public record InternalScheduleItem(
        @Schema(description = "일정 ID", example = "12") Long id,
        @Schema(description = "일정 이름", example = "오리엔테이션") String name,
        @Schema(description = "일정 설명", example = "오리엔테이션") String description,
        @Schema(description = "일정 날짜", example = "2026-11-07") LocalDate date,
        @Schema(description = "일정 시간", example = "14:00") LocalTime scheduleTime,
        @Schema(description = "출석 기록 수", example = "3") long attendanceCount) {}
