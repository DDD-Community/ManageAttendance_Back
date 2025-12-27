package com.ddd.manage_attendance.domain.team.exception;

import com.ddd.manage_attendance.core.exception.BaseException;

public class TeamNotFoundException extends BaseException {

    public TeamNotFoundException() {
        super("존재하지 않는 팀입니다.");
    }
}
