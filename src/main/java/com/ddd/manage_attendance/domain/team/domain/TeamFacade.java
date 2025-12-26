package com.ddd.manage_attendance.domain.team.domain;

import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import com.ddd.manage_attendance.domain.team.api.dto.TeamResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamFacade {
    private final UserService userService;
    private final TeamService teamService;

    @Transactional(readOnly = true)
    public List<TeamResponse> getCurrentGenerationTeams(Long userId) {
        // TODO: 운영진 확인 필요
        final User user = userService.getUser(userId);
        return TeamResponse.fromList(teamService.findAllByGenerationId(user.getGenerationId()));
    }
}
