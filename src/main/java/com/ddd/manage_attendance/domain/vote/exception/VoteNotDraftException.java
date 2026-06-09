package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class VoteNotDraftException extends BaseException {

    public VoteNotDraftException() {
        super(ErrorCode.VOTE_NOT_DRAFT);
    }
}
