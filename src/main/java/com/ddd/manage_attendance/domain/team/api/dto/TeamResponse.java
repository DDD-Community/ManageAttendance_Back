package com.ddd.manage_attendance.domain.team.api.dto;

import com.ddd.manage_attendance.domain.team.domain.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;

public record TeamResponse(
        @Schema(description = "팀 Id", example = "1") Long teamId,
        @Schema(description = "팀 이름", example = "web1팀") String name,
        @Schema(description = "서비스명", example = "디톡스메이트") String serviceName) {

    public static TeamResponse from(final Team team) {
        return new TeamResponse(team.getId(), team.getName(), team.getServiceName());
    }

    public static List<TeamResponse> fromList(final List<Team> teams) {
        return teams.stream().map(TeamResponse::from).collect(Collectors.toList());
    }
}
