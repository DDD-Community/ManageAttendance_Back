package com.ddd.manage_attendance.domain.team.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class TeamDuplicatedException extends BaseException {
    public TeamDuplicatedException() {
        super(ErrorCode.TEAM_DUPLICATED);
    }
}
