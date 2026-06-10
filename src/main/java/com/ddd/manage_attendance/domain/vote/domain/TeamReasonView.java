package com.ddd.manage_attendance.domain.vote.domain;

/** 팀 투표 부문별 작성 사유(익명). {@code SELECT new ...} 생성자 프로젝션 대상. */
public record TeamReasonView(String categoryId, String reason) {}
