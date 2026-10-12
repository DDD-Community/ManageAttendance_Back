package com.ddd.manage_attendance.domain.attendance.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record InternalScheduleItem(Long id, String name, LocalDate date, LocalTime scheduleTime) {}
