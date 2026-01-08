package com.ddd.manage_attendance.domain.schedule.domain;

import com.ddd.manage_attendance.core.exception.BaseException;

public class NoScheduleException extends BaseException {
    public NoScheduleException() {
        super("출석일이 아닙니다.");
    }
}
