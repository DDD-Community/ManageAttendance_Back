package com.ddd.manage_attendance.domain.vote.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 멤버의 투표 제출(팀 투표 + 피드백 원자 제출). */
@Schema(title = "[투표] 투표 제출 요청 DTO")
public record VoteSubmitRequest(
        @Schema(description = "팀 투표 부문별 응답") List<TeamVoteCategoryAnswer> teamVote,
        @Schema(description = "피드백 질문별 응답") List<FeedbackQuestionAnswer> feedback) {

    @Schema(title = "팀 투표 부문 응답")
    public record TeamVoteCategoryAnswer(
            @Schema(description = "부문 ID", example = "PLANNING") String categoryId,
            @Schema(description = "선택한 팀 Id 목록(본인 팀 제외, 최대 N개)") List<Long> teamIds,
            @Schema(description = "작성 사유") String reason) {}

    @Schema(title = "피드백 질문 응답")
    public record FeedbackQuestionAnswer(
            @Schema(description = "질문 ID", example = "BEST_CURRICULUM") String questionId,
            @Schema(description = "선택한 옵션 ID 목록(MULTI_SELECT)") List<String> optionIds,
            @Schema(description = "텍스트 응답(LONG_TEXT)") String textValue,
            @Schema(description = "예/아니오 응답(BOOLEAN)") Boolean boolValue) {}

    public List<TeamVoteCategoryAnswer> teamVoteOrEmpty() {
        return teamVote == null ? List.of() : teamVote;
    }

    public List<FeedbackQuestionAnswer> feedbackOrEmpty() {
        return feedback == null ? List.of() : feedback;
    }
}
