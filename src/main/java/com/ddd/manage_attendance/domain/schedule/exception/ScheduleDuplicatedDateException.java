package com.ddd.manage_attendance.domain.schedule.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class ScheduleDuplicatedDateException extends BaseException {
    public ScheduleDuplicatedDateException() {
        super(ErrorCode.SCHEDULE_DUPLICATED_DATE);
    }
}
