package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class VoteNotFoundException extends BaseException {

    public VoteNotFoundException() {
        super(ErrorCode.VOTE_NOT_FOUND);
    }
}
