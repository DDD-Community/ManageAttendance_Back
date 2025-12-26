package com.ddd.manage_attendance.domain.attendance.domain;

import com.ddd.manage_attendance.core.exception.BaseException;

public class NotUserAttendanceException extends BaseException {
    public NotUserAttendanceException() {
        super("다른 팀원의 출석을 수정 하고 있습니다.");
    }
}
