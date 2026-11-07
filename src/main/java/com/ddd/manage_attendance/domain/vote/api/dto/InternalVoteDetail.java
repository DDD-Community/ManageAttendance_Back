package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.domain.vote.domain.Vote;
import com.ddd.manage_attendance.domain.vote.domain.VoteStatus;
import java.time.LocalDateTime;

public record InternalVoteDetail(
        Long voteId,
        Long generationId,
        String title,
        VoteStatus status,
        int templateVersion,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        LocalDateTime createdDate,
        long responseCount,
        TeamVoteTemplate teamVoteTemplate,
        FeedbackTemplate feedbackTemplate) {
    public static InternalVoteDetail of(final Vote vote, final long count) {
        return new InternalVoteDetail(
                vote.getId(),
                vote.getGenerationId(),
                vote.getTitle(),
                vote.getStatus(),
                vote.getTemplateVersion(),
                vote.getOpenedAt(),
                vote.getClosedAt(),
                vote.getCreatedDate(),
                count,
                vote.getTeamVoteTemplate(),
                vote.getFeedbackTemplate());
    }
}
