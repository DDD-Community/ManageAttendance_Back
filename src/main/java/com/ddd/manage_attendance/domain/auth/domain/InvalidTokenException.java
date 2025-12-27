package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.exception.BaseException;

public class InvalidTokenException extends BaseException {

    public InvalidTokenException() {
        super("유효하지 않은 토큰입니다.");
    }
}
