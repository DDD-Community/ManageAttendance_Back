package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.vote.domain.Vote;
import com.ddd.manage_attendance.domain.vote.domain.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

@Schema(title = "[투표] 팀 투표 템플릿 응답 DTO")
public record TeamVoteTemplateResponse(
        @Schema(description = "템플릿 버전") int templateVersion,
        @Schema(description = "투표 상태") VoteStatus status,
        @Schema(description = "팀 투표 템플릿 정의") TeamVoteTemplate template,
        @Schema(description = "선택 대상 팀 목록(본인 팀 포함, isOwnTeam 으로 구분)") List<TeamItem> teams) {

    @Schema(title = "팀 항목")
    public record TeamItem(
            @Schema(description = "팀 Id") Long teamId,
            @Schema(description = "팀 이름", example = "iOS 1팀") String name,
            @Schema(description = "서비스명", example = "PICKFLOW") String serviceName,
            @Schema(description = "본인 팀 여부(true 면 선택 불가)") boolean isOwnTeam) {}

    public static TeamVoteTemplateResponse from(
            final Vote vote, final List<Team> teams, final Long ownTeamId) {
        final List<TeamItem> items =
                teams.stream()
                        .map(
                                team ->
                                        new TeamItem(
                                                team.getId(),
                                                team.getName(),
                                                team.getServiceName(),
                                                Objects.equals(team.getId(), ownTeamId)))
                        .toList();
        return new TeamVoteTemplateResponse(
                vote.getTemplateVersion(), vote.getStatus(), vote.getTeamVoteTemplate(), items);
    }
}
