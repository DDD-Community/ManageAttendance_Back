package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class VoteTemplateStructureChangedException extends BaseException {
    public VoteTemplateStructureChangedException() {
        super(ErrorCode.VOTE_TEMPLATE_STRUCTURE_CHANGED);
    }
}
