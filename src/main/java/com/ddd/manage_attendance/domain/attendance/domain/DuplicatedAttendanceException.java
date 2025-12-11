package com.ddd.manage_attendance.domain.attendance.domain;

import com.ddd.manage_attendance.core.exception.BaseException;

public class DuplicatedAttendanceException extends BaseException {
    public DuplicatedAttendanceException() {
        super("이미 출석을 완료했습니다.");
    }
}
