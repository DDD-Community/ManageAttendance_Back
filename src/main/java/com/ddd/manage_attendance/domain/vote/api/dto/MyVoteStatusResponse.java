package com.ddd.manage_attendance.domain.vote.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[투표] 내 참여 여부 조회 응답 DTO")
public record MyVoteStatusResponse(
        @Schema(description = "투표 Id", example = "1") Long voteId,
        @Schema(description = "내가 이미 참여했는지 여부") boolean responded) {

    public static MyVoteStatusResponse of(final Long voteId, final boolean responded) {
        return new MyVoteStatusResponse(voteId, responded);
    }
}
