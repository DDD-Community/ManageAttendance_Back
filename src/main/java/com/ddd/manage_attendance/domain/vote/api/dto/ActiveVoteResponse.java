package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.domain.vote.domain.Vote;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[투표] 진행 중 투표 조회 응답 DTO")
public record ActiveVoteResponse(
        @Schema(description = "투표 Id", example = "1") Long voteId,
        @Schema(description = "투표 제목", example = "DDD 13기 최종 투표") String title,
        @Schema(description = "내가 이미 참여했는지 여부 (true 면 완료 화면 노출)") boolean alreadyResponded) {

    public static ActiveVoteResponse of(final Vote vote, final boolean alreadyResponded) {
        return new ActiveVoteResponse(vote.getId(), vote.getTitle(), alreadyResponded);
    }
}
