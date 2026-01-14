package com.ddd.manage_attendance.domain.team.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class TeamNotFoundException extends BaseException {

    public TeamNotFoundException() {
        super(ErrorCode.TEAM_NOT_FOUND);
    }
}
