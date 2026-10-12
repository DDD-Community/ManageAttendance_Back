package com.ddd.manage_attendance.domain.generation.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class GenerationInUseException extends BaseException {
    public GenerationInUseException() {
        super(ErrorCode.GENERATION_IN_USE);
    }
}
