package com.ddd.manage_attendance.domain.team.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.domain.auth.domain.UserRepository;
import com.ddd.manage_attendance.domain.auth.domain.UserRole;
import com.ddd.manage_attendance.domain.generation.domain.GenerationRepository;
import com.ddd.manage_attendance.domain.team.api.dto.InternalTeamItem;
import com.ddd.manage_attendance.domain.team.exception.TeamDuplicatedException;
import com.ddd.manage_attendance.domain.team.exception.TeamInUseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalTeamFacade {
    private final TeamRepository teams;
    private final UserRepository users;
    private final GenerationRepository generations;

    @Transactional(readOnly = true)
    public List<InternalTeamItem> listTeams(final Long generationId) {
        requireGeneration(generationId);
        return teams.findAllByGenerationIdOrderByNameAsc(generationId).stream()
                .map(
                        team ->
                                new InternalTeamItem(
                                        team.getId(),
                                        team.getName(),
                                        team.getServiceName(),
                                        users.countByTeamIdAndRole(team.getId(), UserRole.MEMBER)))
                .toList();
    }

    @Transactional
    public InternalTeamItem createTeam(
            final Long generationId, final String rawName, final String serviceName) {
        requireGeneration(generationId);
        final String name = rawName.trim();
        if (teams.existsByNameAndGenerationId(name, generationId)) {
            throw new TeamDuplicatedException();
        }
        try {
            final Team team = teams.saveAndFlush(Team.createTeam(name, generationId, serviceName));
            return new InternalTeamItem(team.getId(), team.getName(), team.getServiceName(), 0);
        } catch (DataIntegrityViolationException exception) {
            throw new TeamDuplicatedException();
        }
    }

    @Transactional
    public void deleteTeam(final Long id) {
        final Team team = teams.findById(id).orElseThrow(DataNotFoundException::new);
        if (users.existsByTeamId(id)) {
            throw new TeamInUseException();
        }
        teams.delete(team);
    }

    private void requireGeneration(final Long id) {
        if (!generations.existsById(id)) {
            throw new DataNotFoundException();
        }
    }
}
