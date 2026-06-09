package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class VoteNotOpenException extends BaseException {

    public VoteNotOpenException() {
        super(ErrorCode.VOTE_NOT_OPEN);
    }
}
