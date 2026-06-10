package com.ddd.manage_attendance.domain.vote.domain;

/** 팀 투표 집계 행(부문별 팀 득표수). {@code SELECT new ...} 생성자 프로젝션 대상. */
public record TeamVoteTally(String categoryId, Long teamId, Long voteCount) {}
