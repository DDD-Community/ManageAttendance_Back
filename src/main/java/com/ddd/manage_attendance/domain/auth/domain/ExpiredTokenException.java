package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.exception.BaseException;

public class ExpiredTokenException extends BaseException {

    public ExpiredTokenException() {
        super("만료된 토큰입니다. 다시 로그인해주세요.");
    }
}
