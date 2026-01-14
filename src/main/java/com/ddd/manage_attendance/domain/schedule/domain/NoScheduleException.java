package com.ddd.manage_attendance.domain.schedule.domain;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class NoScheduleException extends BaseException {
    public NoScheduleException() {
        super(ErrorCode.SCHEDULE_NOT_ATTENDANCE_DAY);
    }
}
