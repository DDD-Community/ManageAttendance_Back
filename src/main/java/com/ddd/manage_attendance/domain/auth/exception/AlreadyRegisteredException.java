package com.ddd.manage_attendance.domain.auth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;

public class AlreadyRegisteredException extends BaseException {
    public AlreadyRegisteredException() {
        super("이미 가입된 회원 정보입니다");
    }
}
