package com.ddd.manage_attendance.core.exception;

public class DataNotFoundException extends BaseException {

    public DataNotFoundException() {
        super("존재하지 않는 데이터입니다.");
    }
}
