package com.ddd.manage_attendance.domain.auth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class GenerationMismatchException extends BaseException {

    public GenerationMismatchException() {
        super(ErrorCode.AUTH_GENERATION_MISMATCH);
    }
}
