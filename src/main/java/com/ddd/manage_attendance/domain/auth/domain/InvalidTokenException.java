package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class InvalidTokenException extends BaseException {

    public InvalidTokenException() {
        super(ErrorCode.AUTH_INVALID_TOKEN);
    }
}
