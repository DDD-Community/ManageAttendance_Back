package com.ddd.manage_attendance.domain.team.api.dto;

import com.ddd.manage_attendance.domain.team.domain.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;

public record TeamResponse(
        @Schema(description = "팀 Id", example = "1") Long teamId,
        @Schema(description = "팀 이름", example = "web1팀") String name) {

    public static TeamResponse from(final Team team) {
        return new TeamResponse(team.getId(), team.getName());
    }

    public static List<TeamResponse> fromList(final List<Team> teams) {
        return teams.stream().map(TeamResponse::from).collect(Collectors.toList());
    }
}
