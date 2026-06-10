package com.ddd.manage_attendance.domain.vote.domain;

/** 피드백 BOOLEAN 예/아니오 응답 수 집계. {@code SELECT new ...} 생성자 프로젝션 대상. */
public record FeedbackBoolTally(String questionId, Boolean boolValue, Long count) {}
