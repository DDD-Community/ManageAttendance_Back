package com.ddd.manage_attendance.domain.generation.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.domain.auth.domain.InvitationRepository;
import com.ddd.manage_attendance.domain.auth.domain.UserRepository;
import com.ddd.manage_attendance.domain.auth.domain.UserRole;
import com.ddd.manage_attendance.domain.generation.api.dto.InternalGenerationItem;
import com.ddd.manage_attendance.domain.generation.exception.GenerationDuplicatedException;
import com.ddd.manage_attendance.domain.generation.exception.GenerationInUseException;
import com.ddd.manage_attendance.domain.schedule.domain.ScheduleRepository;
import com.ddd.manage_attendance.domain.team.domain.TeamRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalGenerationFacade {
    private final GenerationRepository generations;
    private final TeamRepository teams;
    private final UserRepository users;
    private final ScheduleRepository schedules;
    private final InvitationRepository invitations;

    @Transactional(readOnly = true)
    public List<InternalGenerationItem> listGenerations() {
        return generations.findAllByOrderByIdDesc().stream()
                .map(
                        generation ->
                                new InternalGenerationItem(
                                        generation.getId(),
                                        generation.getName(),
                                        users.countByGenerationIdAndRole(
                                                generation.getId(), UserRole.MEMBER)))
                .toList();
    }

    @Transactional
    public InternalGenerationItem createGeneration(final String rawName) {
        final String name = rawName.trim();
        if (generations.existsByName(name)) {
            throw new GenerationDuplicatedException();
        }
        try {
            final Generation generation =
                    generations.saveAndFlush(Generation.createGeneration(name));
            return new InternalGenerationItem(generation.getId(), generation.getName(), 0);
        } catch (DataIntegrityViolationException exception) {
            throw new GenerationDuplicatedException();
        }
    }

    @Transactional
    public void deleteGeneration(final Long id) {
        final Generation generation =
                generations.findById(id).orElseThrow(DataNotFoundException::new);
        if (teams.existsByGenerationId(id)
                || users.existsByGenerationId(id)
                || schedules.existsByGenerationId(id)
                || invitations.existsByGenerationId(id)) {
            throw new GenerationInUseException();
        }
        generations.delete(generation);
    }
}
