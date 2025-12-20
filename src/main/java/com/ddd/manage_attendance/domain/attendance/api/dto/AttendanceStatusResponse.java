package com.ddd.manage_attendance.domain.attendance.api.dto;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record AttendanceStatusResponse(
        @Schema(description = "출석 상태 이름", example = "출석") String name,
        @Schema(description = "출석 상태 코드", example = "ATTENDED") String code) {
    public static AttendanceStatusResponse from(final AttendanceStatus status) {
        return new AttendanceStatusResponse(status.name(), status.getDescription());
    }
}
