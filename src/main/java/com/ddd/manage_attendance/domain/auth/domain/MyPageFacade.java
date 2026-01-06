package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.generation.domain.GenerationService;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageFacade {

    private final UserService userService;
    private final GenerationService generationService;
    private final TeamService teamService;

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(final Long userId) {
        final User user = userService.getUser(userId);
        final String generationName = generationService.getGenerationName(user.getGenerationId());
        final String teamName = teamService.getTeamName(user.getTeamId());

        return UserInfoResponse.from(user, generationName, teamName);
    }
}
