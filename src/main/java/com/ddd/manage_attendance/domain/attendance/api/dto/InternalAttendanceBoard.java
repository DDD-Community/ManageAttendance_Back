package com.ddd.manage_attendance.domain.attendance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "기수별 출석 보드 응답")
public record InternalAttendanceBoard(
        @Schema(description = "기수 정보", example = "5기") InternalGenerationInfo generation,
        @Schema(description = "조회 기준 날짜", example = "2026-10-12") LocalDate today,
        @Schema(description = "기수 일정 목록", example = "일정 목록") List<InternalScheduleItem> schedules,
        @Schema(description = "기수 회원 목록", example = "회원 목록") List<InternalMemberItem> members,
        @Schema(description = "회원별 출석 상태 목록", example = "출석 상태 목록")
                List<InternalAttendanceCell> cells) {}
