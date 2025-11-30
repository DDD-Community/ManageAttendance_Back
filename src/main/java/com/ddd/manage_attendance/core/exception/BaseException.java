package com.ddd.manage_attendance.core.exception;

public class BaseException extends RuntimeException {
    public BaseException() {
        super("알 수 없는 오류가 발생했습니다.");
    }

    public BaseException(String message) {
        super(message);
    }
}
