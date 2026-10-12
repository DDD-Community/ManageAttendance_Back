package com.ddd.manage_attendance.domain.team.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.domain.auth.domain.UserRepository;
import com.ddd.manage_attendance.domain.auth.domain.UserRole;
import com.ddd.manage_attendance.domain.generation.domain.GenerationRepository;
import com.ddd.manage_attendance.domain.team.api.dto.InternalTeamItem;
import com.ddd.manage_attendance.domain.team.exception.TeamDuplicatedException;
import com.ddd.manage_attendance.domain.team.exception.TeamInUseException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalTeamFacadeTest {
    private static final Long GENERATION_ID = 13L;
    private static final Long TEAM_ID = 7L;

    @Mock private TeamRepository teams;
    @Mock private UserRepository users;
    @Mock private GenerationRepository generations;
    @InjectMocks private InternalTeamFacade facade;

    @Test
    @DisplayName("팀에 사용자가 있으면 삭제할 수 없다")
    void deleteTeam_whenUserExists_throws() {
        final Team team = mock(Team.class);
        given(teams.findById(TEAM_ID)).willReturn(Optional.of(team));
        given(users.existsByTeamId(TEAM_ID)).willReturn(true);

        assertThatThrownBy(() -> facade.deleteTeam(TEAM_ID)).isInstanceOf(TeamInUseException.class);
        verify(teams, never()).delete(team);
    }

    @Test
    @DisplayName("팀에 사용자가 없으면 삭제한다")
    void deleteTeam_whenUnused_deletes() {
        final Team team = mock(Team.class);
        given(teams.findById(TEAM_ID)).willReturn(Optional.of(team));

        facade.deleteTeam(TEAM_ID);

        verify(teams).delete(team);
    }

    @Test
    @DisplayName("같은 기수에 같은 팀명이 있으면 중복 예외가 발생한다")
    void createTeam_whenDuplicate_throws() {
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        given(teams.existsByNameAndGenerationId("플랫폼", GENERATION_ID)).willReturn(true);

        assertThatThrownBy(() -> facade.createTeam(GENERATION_ID, " 플랫폼 ", null))
                .isInstanceOf(TeamDuplicatedException.class);
        verify(teams, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("없는 기수에 팀을 만들 수 없다")
    void createTeam_whenGenerationUnknown_throws() {
        assertThatThrownBy(() -> facade.createTeam(GENERATION_ID, "플랫폼", null))
                .isInstanceOf(DataNotFoundException.class);
        verify(teams, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("팀 인원은 MEMBER 역할만 센다")
    void listTeams_countsOnlyMembers() {
        final Team team = mock(Team.class);
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        given(teams.findAllByGenerationIdOrderByNameAsc(GENERATION_ID)).willReturn(List.of(team));
        given(team.getId()).willReturn(TEAM_ID);
        given(team.getName()).willReturn("플랫폼");
        given(users.countByTeamIdAndRole(TEAM_ID, UserRole.MEMBER)).willReturn(3L);

        final List<InternalTeamItem> result = facade.listTeams(GENERATION_ID);

        assertThat(result).singleElement().extracting(InternalTeamItem::memberCount).isEqualTo(3L);
        verify(users).countByTeamIdAndRole(TEAM_ID, UserRole.MEMBER);
    }
}
