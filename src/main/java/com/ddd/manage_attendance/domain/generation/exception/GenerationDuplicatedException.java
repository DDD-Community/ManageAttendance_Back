package com.ddd.manage_attendance.domain.generation.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class GenerationDuplicatedException extends BaseException {
    public GenerationDuplicatedException() {
        super(ErrorCode.GENERATION_DUPLICATED);
    }
}
