package com.ddd.manage_attendance.domain.schedule.domain;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;
import java.time.LocalDate;

public record ScheduleWithAttendanceProjection(
        Long id, AttendanceStatus status, String name, String desc, LocalDate date) {}
