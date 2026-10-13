package com.ddd.manage_attendance.domain.team.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class TeamInUseException extends BaseException {
    public TeamInUseException() {
        super(ErrorCode.TEAM_IN_USE);
    }
}
