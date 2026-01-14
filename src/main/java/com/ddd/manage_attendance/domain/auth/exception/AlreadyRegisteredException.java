package com.ddd.manage_attendance.domain.auth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class AlreadyRegisteredException extends BaseException {
    public AlreadyRegisteredException() {
        super(ErrorCode.AUTH_ALREADY_REGISTERED);
    }
}
