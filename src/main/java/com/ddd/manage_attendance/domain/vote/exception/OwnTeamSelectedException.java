package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class OwnTeamSelectedException extends BaseException {

    public OwnTeamSelectedException() {
        super(ErrorCode.VOTE_OWN_TEAM_SELECTED);
    }
}
