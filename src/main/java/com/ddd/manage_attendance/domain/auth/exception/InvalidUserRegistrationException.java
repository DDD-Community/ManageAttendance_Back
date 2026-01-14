package com.ddd.manage_attendance.domain.auth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class InvalidUserRegistrationException extends BaseException {

    public InvalidUserRegistrationException(String message) {
        super(ErrorCode.AUTH_INVALID_USER_REGISTRATION, message);
    }
}
