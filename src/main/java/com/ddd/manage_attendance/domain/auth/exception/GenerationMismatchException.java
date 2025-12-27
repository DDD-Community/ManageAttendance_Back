package com.ddd.manage_attendance.domain.auth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;

public class GenerationMismatchException extends BaseException {

    public GenerationMismatchException() {
        super("초대 코드의 기수와 요청한 기수가 일치하지 않습니다.");
    }
}
