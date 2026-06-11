package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(title = "[투표] 미참여 멤버 명단 응답 DTO")
public record VoteNonRespondersResponse(
        @Schema(description = "미참여 멤버 수", example = "7") int totalCount,
        @Schema(description = "미참여 멤버 목록") List<NonResponder> members) {

    @Schema(title = "미참여 멤버 항목")
    public record NonResponder(
            @Schema(description = "멤버 Id") Long memberId,
            @Schema(description = "이름", example = "홍길동") String name,
            @Schema(description = "소속 팀명(미배정 시 null)", example = "iOS 1팀") String teamName,
            @Schema(
                            description =
                                    "금일 출석 상태 (ATTENDED/LATE/ABSENT/NONE). 금일 일정이 없거나 미체크면 NONE",
                            example = "ATTENDED")
                    AttendanceStatus todayAttendanceStatus) {}

    public static VoteNonRespondersResponse of(final List<NonResponder> members) {
        return new VoteNonRespondersResponse(members.size(), members);
    }
}
