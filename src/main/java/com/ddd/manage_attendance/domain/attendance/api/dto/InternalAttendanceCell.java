package com.ddd.manage_attendance.domain.attendance.api.dto;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;

public record InternalAttendanceCell(Long memberId, Long scheduleId, AttendanceStatus status) {}
