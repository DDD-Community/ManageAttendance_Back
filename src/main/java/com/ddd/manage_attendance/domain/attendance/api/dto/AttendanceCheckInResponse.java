package com.ddd.manage_attendance.domain.attendance.api.dto;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[출석] 출석 응답 DTO")
public record AttendanceCheckInResponse(
        @Schema(description = "출석 상태 코드 값", example = "ATTENDED || LATE || ABSENT ")
                AttendanceStatus status) {
    public static AttendanceCheckInResponse from(final AttendanceStatus status) {
        return new AttendanceCheckInResponse(status);
    }
}
