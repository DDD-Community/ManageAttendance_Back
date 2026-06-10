package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.domain.vote.domain.Vote;
import com.ddd.manage_attendance.domain.vote.domain.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[투표] 운영진 투표 상세(상태 + 양쪽 템플릿) 응답 DTO")
public record VoteDetailResponse(
        @Schema(description = "투표 Id", example = "1") Long voteId,
        @Schema(description = "투표 제목") String title,
        @Schema(description = "투표 상태 (DRAFT/OPEN/CLOSED)") VoteStatus status,
        @Schema(description = "템플릿 버전") int templateVersion,
        @Schema(description = "팀 투표 템플릿(없으면 null)") TeamVoteTemplate teamVoteTemplate,
        @Schema(description = "참여 경험 피드백 템플릿(없으면 null)") FeedbackTemplate feedbackTemplate) {

    public static VoteDetailResponse from(final Vote vote) {
        return new VoteDetailResponse(
                vote.getId(),
                vote.getTitle(),
                vote.getStatus(),
                vote.getTemplateVersion(),
                vote.getTeamVoteTemplate(),
                vote.getFeedbackTemplate());
    }
}
