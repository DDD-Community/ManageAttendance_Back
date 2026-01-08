package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.exception.BaseException;

public class ManagerOnlyOperationException extends BaseException {

    public ManagerOnlyOperationException() {
        super("운영진 권한이 없는 사용자는 해당 기능을 사용할 수 없습니다.");
    }
}
