package com.ddd.manage_attendance.domain.vote.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 투표 상태머신. DRAFT(작성중) → OPEN(진행중) → CLOSED(종료). 종료 후 재시작 불가(불가역). */
@Getter
@RequiredArgsConstructor
public enum VoteStatus {
    DRAFT("작성중"),
    OPEN("진행중"),
    CLOSED("종료");

    private final String description;
}
