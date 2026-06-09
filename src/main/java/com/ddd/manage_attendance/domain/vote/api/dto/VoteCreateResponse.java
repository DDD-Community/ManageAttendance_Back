package com.ddd.manage_attendance.domain.vote.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[투표] 투표 생성 응답 DTO")
public record VoteCreateResponse(@Schema(description = "생성된 투표 Id", example = "1") Long voteId) {

    public static VoteCreateResponse from(final Long voteId) {
        return new VoteCreateResponse(voteId);
    }
}
