package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class ManagerOnlyOperationException extends BaseException {

    public ManagerOnlyOperationException() {
        super(ErrorCode.MANAGER_ONLY);
    }
}
