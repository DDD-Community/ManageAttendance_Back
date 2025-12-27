package com.ddd.manage_attendance.domain.auth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;

public class InvalidUserRegistrationException extends BaseException {

    public InvalidUserRegistrationException(String message) {
        super(message);
    }
}
