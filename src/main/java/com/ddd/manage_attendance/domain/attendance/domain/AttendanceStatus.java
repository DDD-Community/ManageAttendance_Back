package com.ddd.manage_attendance.domain.attendance.domain;

import lombok.Getter;

@Getter
public enum AttendanceStatus {
    ATTENDED("출석"),
    LATE("지각"),
    ABSENT("결석");

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }
}
