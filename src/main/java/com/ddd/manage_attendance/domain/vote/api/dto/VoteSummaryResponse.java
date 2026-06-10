package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.domain.vote.domain.Vote;
import com.ddd.manage_attendance.domain.vote.domain.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(title = "[투표] 운영진 투표 목록 항목 응답 DTO")
public record VoteSummaryResponse(
        @Schema(description = "투표 Id", example = "1") Long voteId,
        @Schema(description = "투표 제목", example = "DDD 13기 최종 투표") String title,
        @Schema(description = "투표 상태 (DRAFT/OPEN/CLOSED)") VoteStatus status,
        @Schema(description = "투표 시작 시각(미시작 시 null)") LocalDateTime openedAt,
        @Schema(description = "투표 종료 시각(미종료 시 null)") LocalDateTime closedAt,
        @Schema(description = "생성 일자") LocalDateTime createdDate) {

    public static VoteSummaryResponse from(final Vote vote) {
        return new VoteSummaryResponse(
                vote.getId(),
                vote.getTitle(),
                vote.getStatus(),
                vote.getOpenedAt(),
                vote.getClosedAt(),
                vote.getCreatedDate());
    }

    public static List<VoteSummaryResponse> fromList(final List<Vote> votes) {
        return votes.stream().map(VoteSummaryResponse::from).toList();
    }
}
