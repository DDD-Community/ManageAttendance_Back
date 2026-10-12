package com.ddd.manage_attendance.domain.attendance.api.dto;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원과 일정의 출석 상태")
public record InternalAttendanceCell(
        @Schema(description = "회원 ID", example = "1") Long memberId,
        @Schema(description = "일정 ID", example = "1") Long scheduleId,
        @Schema(description = "출석 상태", example = "PRESENT") AttendanceStatus status) {}
