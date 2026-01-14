package com.ddd.manage_attendance.core.exception;

public class DataNotFoundException extends BaseException {

    public DataNotFoundException() {
        super(ErrorCode.DATA_NOT_FOUND);
    }
}
