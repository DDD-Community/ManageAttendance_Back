package com.ddd.manage_attendance.domain.vote.domain;

/** 피드백 MULTI_SELECT 선택지별 응답 수 집계. {@code SELECT new ...} 생성자 프로젝션 대상. */
public record FeedbackOptionTally(String questionId, String optionId, Long count) {}
