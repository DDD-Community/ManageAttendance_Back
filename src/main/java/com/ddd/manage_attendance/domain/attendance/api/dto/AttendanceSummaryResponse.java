package com.ddd.manage_attendance.domain.attendance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AttendanceSummaryResponse(
        @Schema(description = "총 출석 일", example = "1") int totalAttended,
        @Schema(description = "총 지각 일", example = "2") int totalLate,
        @Schema(description = "총 결석 일", example = "3") int totalAbsent) {
    public static AttendanceSummaryResponse from(
            final int totalAttended, final int totalLate, final int totalAbsent) {
        return new AttendanceSummaryResponse(totalAttended, totalLate, totalAbsent);
    }
}
