package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import java.time.LocalDateTime;
import java.util.List;

public record InternalVoteResponseExport(
        Long voteId,
        String title,
        int templateVersion,
        TeamVoteTemplate teamVoteTemplate,
        FeedbackTemplate feedbackTemplate,
        List<Response> responses) {
    public record Response(
            Long responseId,
            Long memberId,
            String memberName,
            Long teamId,
            String teamName,
            int templateVersion,
            LocalDateTime submittedAt,
            List<TeamVote> teamVote,
            List<Feedback> feedback) {}

    public record TeamVote(
            String categoryId, List<Long> teamIds, List<String> teamNames, String reason) {}

    public record Feedback(
            String questionId, List<String> optionIds, String textValue, Boolean boolValue) {}
}
