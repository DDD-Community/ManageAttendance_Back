package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class VoteAlreadyOpenException extends BaseException {

    public VoteAlreadyOpenException() {
        super(ErrorCode.VOTE_ALREADY_OPEN);
    }
}
