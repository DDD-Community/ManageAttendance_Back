package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[투표] 템플릿 수정 요청 DTO (DRAFT 상태만)")
public record VoteTemplateUpdateRequest(
        @Schema(description = "팀 투표 템플릿") TeamVoteTemplate teamVoteTemplate,
        @Schema(description = "참여 경험 피드백 템플릿") FeedbackTemplate feedbackTemplate) {}
