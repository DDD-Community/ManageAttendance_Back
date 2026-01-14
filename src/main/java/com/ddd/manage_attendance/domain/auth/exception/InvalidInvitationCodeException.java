package com.ddd.manage_attendance.domain.auth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class InvalidInvitationCodeException extends BaseException {

    public InvalidInvitationCodeException() {
        super(ErrorCode.AUTH_INVALID_INVITATION_CODE);
    }

    public InvalidInvitationCodeException(String message) {
        super(ErrorCode.AUTH_INVALID_INVITATION_CODE, message);
    }
}
