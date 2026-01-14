package com.ddd.manage_attendance.domain.attendance.domain;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class NotUserAttendanceException extends BaseException {
    public NotUserAttendanceException() {
        super(ErrorCode.ATTENDANCE_NOT_USER);
    }
}
