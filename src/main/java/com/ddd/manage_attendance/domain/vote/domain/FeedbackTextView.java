package com.ddd.manage_attendance.domain.vote.domain;

/** 피드백 LONG_TEXT 작성 응답(익명). {@code SELECT new ...} 생성자 프로젝션 대상. */
public record FeedbackTextView(String questionId, String textValue) {}
