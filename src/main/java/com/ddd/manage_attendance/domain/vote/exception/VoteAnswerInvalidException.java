package com.ddd.manage_attendance.domain.vote.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

/** 제출된 응답이 템플릿 제약(선택 개수/글자수/필수 등)을 위반한 경우. 구체 사유를 커스텀 메시지로 전달한다. */
public class VoteAnswerInvalidException extends BaseException {

    public VoteAnswerInvalidException(final String message) {
        super(ErrorCode.VOTE_ANSWER_INVALID, message);
    }
}
