package com.ddd.manage_attendance.domain.auth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class InvitationDuplicatedException extends BaseException {
    public InvitationDuplicatedException() {
        super(ErrorCode.INVITATION_DUPLICATED);
    }
}
