package com.ddd.manage_attendance.domain.auth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;

public class InvalidInvitationCodeException extends BaseException {

    public InvalidInvitationCodeException() {
        super("유효하지 않은 초대 코드입니다.");
    }

    public InvalidInvitationCodeException(String message) {
        super(message);
    }
}
