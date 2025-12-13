package com.ddd.manage_attendance.domain.attendance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[출석] 출석 요청 DTO")
public record AttendanceCheckInRequest(
        @Schema(description = "QR 코드 값", example = "DDD|M|....") String qrCode) {}
