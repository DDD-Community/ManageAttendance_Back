package com.ddd.manage_attendance.domain.auth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class InvitationCodeGenerationFailedException extends BaseException {
    public InvitationCodeGenerationFailedException() {
        super(ErrorCode.INVITATION_CODE_GENERATION_FAILED);
    }
}
