package com.ddd.manage_attendance.domain.generation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ddd.manage_attendance.core.exception.ErrorCode;
import com.ddd.manage_attendance.domain.auth.domain.InvitationRepository;
import com.ddd.manage_attendance.domain.auth.domain.UserRepository;
import com.ddd.manage_attendance.domain.generation.api.dto.InternalGenerationItem;
import com.ddd.manage_attendance.domain.generation.exception.GenerationDuplicatedException;
import com.ddd.manage_attendance.domain.generation.exception.GenerationInUseException;
import com.ddd.manage_attendance.domain.schedule.domain.ScheduleRepository;
import com.ddd.manage_attendance.domain.team.domain.TeamRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class InternalGenerationFacadeTest {
    private static final Long GENERATION_ID = 13L;

    @Mock private GenerationRepository generations;
    @Mock private TeamRepository teams;
    @Mock private UserRepository users;
    @Mock private ScheduleRepository schedules;
    @Mock private InvitationRepository invitations;
    @InjectMocks private InternalGenerationFacade facade;

    @Test
    @DisplayName("팀이 참조하는 기수는 409로 삭제를 거부한다")
    void deleteGeneration_whenTeamExists_throwsConflict() {
        givenGeneration();
        given(teams.existsByGenerationId(GENERATION_ID)).willReturn(true);

        assertDeleteBlocked();
    }

    @Test
    @DisplayName("회원이 참조하는 기수는 409로 삭제를 거부한다")
    void deleteGeneration_whenUserExists_throwsConflict() {
        givenGeneration();
        given(users.existsByGenerationId(GENERATION_ID)).willReturn(true);

        assertDeleteBlocked();
    }

    @Test
    @DisplayName("일정이 참조하는 기수는 409로 삭제를 거부한다")
    void deleteGeneration_whenScheduleExists_throwsConflict() {
        givenGeneration();
        given(schedules.existsByGenerationId(GENERATION_ID)).willReturn(true);

        assertDeleteBlocked();
    }

    @Test
    @DisplayName("초대코드가 참조하는 기수는 409로 삭제를 거부한다")
    void deleteGeneration_whenInvitationExists_throwsConflict() {
        givenGeneration();
        given(invitations.existsByGenerationId(GENERATION_ID)).willReturn(true);

        assertDeleteBlocked();
    }

    @Test
    @DisplayName("기수에 참조가 없으면 삭제한다")
    void deleteGeneration_whenUnused_deletes() {
        final Generation generation = givenGeneration();

        facade.deleteGeneration(GENERATION_ID);

        verify(generations).delete(generation);
    }

    @Test
    @DisplayName("이미 존재하는 기수 이름은 생성 전에 중복 오류가 발생한다")
    void createGeneration_whenNameExists_throwsDuplicate() {
        given(generations.existsByName("13기")).willReturn(true);

        assertThatThrownBy(() -> facade.createGeneration(" 13기 "))
                .isInstanceOf(GenerationDuplicatedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENERATION_DUPLICATED);
        verify(generations, never()).saveAndFlush(any(Generation.class));
    }

    @Test
    @DisplayName("저장 중 유니크 제약 충돌도 기수 중복 오류로 변환한다")
    void createGeneration_whenSaveConflicts_throwsDuplicate() {
        given(generations.saveAndFlush(any(Generation.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> facade.createGeneration("13기"))
                .isInstanceOf(GenerationDuplicatedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENERATION_DUPLICATED);
    }

    @Test
    @DisplayName("새 기수를 생성하면 회원 수 0을 반환한다")
    void createGeneration_whenUnique_returnsCreatedItem() {
        final Generation generation = mock(Generation.class);
        given(generation.getId()).willReturn(GENERATION_ID);
        given(generation.getName()).willReturn("13기");
        given(generations.saveAndFlush(any(Generation.class))).willReturn(generation);

        final InternalGenerationItem item = facade.createGeneration(" 13기 ");

        assertThat(item).isEqualTo(new InternalGenerationItem(GENERATION_ID, "13기", 0));
    }

    private Generation givenGeneration() {
        final Generation generation = mock(Generation.class);
        given(generations.findById(GENERATION_ID)).willReturn(Optional.of(generation));
        return generation;
    }

    private void assertDeleteBlocked() {
        assertThatThrownBy(() -> facade.deleteGeneration(GENERATION_ID))
                .isInstanceOf(GenerationInUseException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENERATION_IN_USE);
        assertThat(ErrorCode.GENERATION_IN_USE.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(generations, never()).delete(any(Generation.class));
    }
}
