package com.ddd.manage_attendance.domain.schedule.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class ScheduleDateConflictException extends BaseException {
    public ScheduleDateConflictException() {
        super(ErrorCode.SCHEDULE_DATE_CONFLICT);
    }
}
