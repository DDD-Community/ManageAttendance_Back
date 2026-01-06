package com.ddd.manage_attendance.domain.team.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public List<Team> findAllByGenerationId(Long generationId) {
        return teamRepository.findAllByGenerationId(generationId);
    }

    @Transactional(readOnly = true)
    public Team findById(Long teamId) {
        return teamRepository.findById(teamId).orElseThrow(DataNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public String getTeamName(final Long teamId) {
        if (teamId == null) {
            return null;
        }
        return teamRepository.findById(teamId).map(Team::getName).orElse(null);
    }
}
