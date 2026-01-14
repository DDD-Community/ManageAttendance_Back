package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class ExpiredTokenException extends BaseException {

    public ExpiredTokenException() {
        super(ErrorCode.AUTH_EXPIRED_TOKEN);
    }
}
