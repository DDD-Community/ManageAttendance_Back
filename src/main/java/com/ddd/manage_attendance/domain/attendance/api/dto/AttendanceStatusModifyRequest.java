package com.ddd.manage_attendance.domain.attendance.api.dto;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[출석] 출석 수정 요청 DTO")
public record AttendanceStatusModifyRequest(
        @Schema(description = "출석 상태 값", example = "LATE") AttendanceStatus status,
        @Schema(description = "팀원 id", example = "1L") Long userId) {}
