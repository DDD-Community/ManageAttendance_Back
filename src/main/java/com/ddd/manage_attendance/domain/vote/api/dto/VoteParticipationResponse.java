package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.domain.vote.domain.Vote;
import com.ddd.manage_attendance.domain.vote.domain.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[투표] 운영진 투표 관리(상태 + 참여 현황) 응답 DTO")
public record VoteParticipationResponse(
        @Schema(description = "투표 Id", example = "1") Long voteId,
        @Schema(description = "투표 제목", example = "DDD 13기 최종 투표") String title,
        @Schema(description = "투표 상태 (DRAFT/OPEN/CLOSED)") VoteStatus status,
        @Schema(description = "대상 멤버 수(운영진 제외)", example = "42") int totalMembers,
        @Schema(description = "참여한 멤버 수", example = "35") int respondedMembers,
        @Schema(description = "참여율(%) 정수 반올림", example = "83") int participationRate) {

    public static VoteParticipationResponse of(
            final Vote vote, final int totalMembers, final int respondedMembers) {
        final int rate =
                totalMembers == 0 ? 0 : (int) Math.round((respondedMembers * 100.0) / totalMembers);
        return new VoteParticipationResponse(
                vote.getId(),
                vote.getTitle(),
                vote.getStatus(),
                totalMembers,
                respondedMembers,
                rate);
    }
}
