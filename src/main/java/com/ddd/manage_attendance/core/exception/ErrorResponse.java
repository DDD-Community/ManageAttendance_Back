package com.ddd.manage_attendance.core.exception;

public record ErrorResponse(String code, String message, String detail) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }

    public static ErrorResponse of(String code, String message, String detail) {
        return new ErrorResponse(code, message, detail);
    }
}
