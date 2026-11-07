package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.domain.vote.domain.Vote;
import com.ddd.manage_attendance.domain.vote.domain.VoteStatus;
import java.time.LocalDateTime;

public record InternalVoteSummary(
        Long voteId,
        Long generationId,
        String title,
        VoteStatus status,
        int templateVersion,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        LocalDateTime createdDate,
        long responseCount) {
    public static InternalVoteSummary of(final Vote vote, final long count) {
        return new InternalVoteSummary(
                vote.getId(),
                vote.getGenerationId(),
                vote.getTitle(),
                vote.getStatus(),
                vote.getTemplateVersion(),
                vote.getOpenedAt(),
                vote.getClosedAt(),
                vote.getCreatedDate(),
                count);
    }
}
