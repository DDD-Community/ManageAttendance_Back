package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class VoteManagerNotAllowedException extends BaseException {

    public VoteManagerNotAllowedException() {
        super(ErrorCode.VOTE_MANAGER_NOT_ALLOWED);
    }
}
