package com.ddd.manage_attendance.domain.attendance.domain;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class DuplicatedAttendanceException extends BaseException {
    public DuplicatedAttendanceException() {
        super(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
    }
}
