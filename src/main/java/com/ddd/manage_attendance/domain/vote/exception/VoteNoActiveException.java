package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class VoteNoActiveException extends BaseException {

    public VoteNoActiveException() {
        super(ErrorCode.VOTE_NO_ACTIVE);
    }
}
