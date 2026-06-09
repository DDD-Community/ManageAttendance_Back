package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.domain.vote.domain.Vote;
import com.ddd.manage_attendance.domain.vote.domain.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[투표] 참여 경험 피드백 템플릿 응답 DTO")
public record FeedbackTemplateResponse(
        @Schema(description = "템플릿 버전") int templateVersion,
        @Schema(description = "투표 상태") VoteStatus status,
        @Schema(description = "피드백 템플릿 정의") FeedbackTemplate template) {

    public static FeedbackTemplateResponse from(final Vote vote) {
        return new FeedbackTemplateResponse(
                vote.getTemplateVersion(), vote.getStatus(), vote.getFeedbackTemplate());
    }
}
