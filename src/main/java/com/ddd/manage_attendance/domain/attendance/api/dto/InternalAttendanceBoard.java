package com.ddd.manage_attendance.domain.attendance.api.dto;

import java.time.LocalDate;
import java.util.List;

public record InternalAttendanceBoard(
        InternalGenerationInfo generation,
        LocalDate today,
        List<InternalScheduleItem> schedules,
        List<InternalMemberItem> members,
        List<InternalAttendanceCell> cells) {}
