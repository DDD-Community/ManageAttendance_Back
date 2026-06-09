package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class VoteAlreadyRespondedException extends BaseException {

    public VoteAlreadyRespondedException() {
        super(ErrorCode.VOTE_ALREADY_RESPONDED);
    }
}
