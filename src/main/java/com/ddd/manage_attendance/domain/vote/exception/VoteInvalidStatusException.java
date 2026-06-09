package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class VoteInvalidStatusException extends BaseException {

    public VoteInvalidStatusException() {
        super(ErrorCode.VOTE_INVALID_STATUS);
    }
}
