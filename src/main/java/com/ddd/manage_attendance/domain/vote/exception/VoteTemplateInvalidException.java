package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

/** 생성/수정 시 투표 템플릿이 구조 제약(필수/중복/범위 등)을 위반한 경우. 구체 사유를 커스텀 메시지로 전달한다. */
public class VoteTemplateInvalidException extends BaseException {

    public VoteTemplateInvalidException(final String message) {
        super(ErrorCode.VOTE_TEMPLATE_INVALID, message);
    }
}
