package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[투표] 투표 생성 요청 DTO")
public record VoteCreateRequest(
        @Schema(description = "기수 Id", example = "1") Long generationId,
        @Schema(description = "투표 제목", example = "DDD 13기 최종 투표") String title,
        @Schema(description = "팀 투표 템플릿") TeamVoteTemplate teamVoteTemplate,
        @Schema(description = "참여 경험 피드백 템플릿") FeedbackTemplate feedbackTemplate) {}
