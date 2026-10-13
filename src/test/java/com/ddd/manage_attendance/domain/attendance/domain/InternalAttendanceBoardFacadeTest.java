package com.ddd.manage_attendance.domain.attendance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.core.util.TimeProvider;
import com.ddd.manage_attendance.domain.attendance.api.dto.InternalAttendanceBoard;
import com.ddd.manage_attendance.domain.attendance.api.dto.InternalAttendanceCell;
import com.ddd.manage_attendance.domain.auth.domain.JobRole;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserRepository;
import com.ddd.manage_attendance.domain.auth.domain.UserRole;
import com.ddd.manage_attendance.domain.generation.domain.Generation;
import com.ddd.manage_attendance.domain.generation.domain.GenerationRepository;
import com.ddd.manage_attendance.domain.schedule.domain.Schedule;
import com.ddd.manage_attendance.domain.schedule.domain.ScheduleRepository;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalAttendanceBoardFacadeTest {
    private static final Long GENERATION_ID = 13L;
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 12);

    @Mock private GenerationRepository generations;
    @Mock private ScheduleRepository schedules;
    @Mock private TeamRepository teams;
    @Mock private UserRepository users;
    @Mock private AttendanceRepository attendances;
    @Mock private TimeProvider timeProvider;
    @InjectMocks private InternalAttendanceBoardFacade facade;

    @Test
    @DisplayName("기존 출석 상태를 유지하고 지난 일정은 결석, 오늘과 미래 일정은 미집계한다")
    void attendanceBoard_buildsDenseCellsWithEffectiveStatuses() {
        final Schedule past = schedule(1L, TODAY.minusDays(1));
        final Schedule today = schedule(2L, TODAY);
        final Schedule future = schedule(3L, TODAY.plusDays(1));
        final Schedule futureWithRow = schedule(4L, TODAY.plusDays(2));
        final User member = member(10L, "김길동", null);
        givenGeneration();
        given(schedules.findAllByGenerationIdOrderByDateAsc(GENERATION_ID))
                .willReturn(List.of(past, today, future, futureWithRow));
        given(teams.findAllByGenerationId(GENERATION_ID)).willReturn(List.of());
        given(users.findAllByGenerationIdAndRole(GENERATION_ID, UserRole.MEMBER))
                .willReturn(List.of(member));
        given(attendances.findAllByScheduleIdIn(List.of(1L, 2L, 3L, 4L)))
                .willReturn(
                        List.of(
                                Attendance.create(10L, 1L, AttendanceStatus.NONE),
                                Attendance.create(10L, 4L, AttendanceStatus.ATTENDED)));
        given(timeProvider.nowDate()).willReturn(TODAY);

        final InternalAttendanceBoard board = facade.attendanceBoard(GENERATION_ID);

        assertThat(board.today()).isEqualTo(TODAY);
        assertThat(board.cells())
                .containsExactly(
                        new InternalAttendanceCell(10L, 1L, AttendanceStatus.NONE),
                        new InternalAttendanceCell(10L, 2L, AttendanceStatus.NONE),
                        new InternalAttendanceCell(10L, 3L, AttendanceStatus.NONE),
                        new InternalAttendanceCell(10L, 4L, AttendanceStatus.ATTENDED));
    }

    @Test
    @DisplayName("출석 행이 없는 지난 일정은 결석으로 표시한다")
    void attendanceBoard_marksMissingPastAttendanceAbsent() {
        final Schedule past = schedule(1L, TODAY.minusDays(1));
        final User member = member(10L, "김길동", null);
        givenGeneration();
        given(schedules.findAllByGenerationIdOrderByDateAsc(GENERATION_ID))
                .willReturn(List.of(past));
        given(teams.findAllByGenerationId(GENERATION_ID)).willReturn(List.of());
        given(users.findAllByGenerationIdAndRole(GENERATION_ID, UserRole.MEMBER))
                .willReturn(List.of(member));
        given(attendances.findAllByScheduleIdIn(List.of(1L))).willReturn(List.of());
        given(timeProvider.nowDate()).willReturn(TODAY);

        final InternalAttendanceBoard board = facade.attendanceBoard(GENERATION_ID);

        assertThat(board.cells())
                .containsExactly(new InternalAttendanceCell(10L, 1L, AttendanceStatus.ABSENT));
    }

    @Test
    @DisplayName("회원만 조회하고 팀 이름, 회원 이름, 아이디 순으로 정렬한다")
    void attendanceBoard_sortsMembersAndExcludesManagers() {
        final Team alpha = team(1L, "A팀");
        final Team beta = team(2L, "B팀");
        final List<User> members =
                List.of(
                        member(4L, "가", null),
                        member(3L, "가", 1L),
                        member(2L, "가", 1L),
                        member(1L, "나", 1L),
                        member(5L, "가", 2L));
        givenGeneration();
        given(schedules.findAllByGenerationIdOrderByDateAsc(GENERATION_ID)).willReturn(List.of());
        given(teams.findAllByGenerationId(GENERATION_ID)).willReturn(List.of(beta, alpha));
        given(users.findAllByGenerationIdAndRole(GENERATION_ID, UserRole.MEMBER))
                .willReturn(members);
        given(timeProvider.nowDate()).willReturn(TODAY);

        final InternalAttendanceBoard board = facade.attendanceBoard(GENERATION_ID);

        assertThat(board.members())
                .extracting(item -> item.id())
                .containsExactly(2L, 3L, 1L, 5L, 4L);
        assertThat(board.members())
                .extracting(item -> item.teamName())
                .containsExactly("A팀", "A팀", "A팀", "B팀", null);
        verify(users).findAllByGenerationIdAndRole(GENERATION_ID, UserRole.MEMBER);
        verify(users, never()).findAllByGenerationIdAndRole(GENERATION_ID, UserRole.MANAGER);
        verifyNoInteractions(attendances);
        assertThat(board.cells()).isEmpty();
    }

    @Test
    @DisplayName("기수가 없으면 조회 실패한다")
    void attendanceBoard_whenGenerationMissing_throws() {
        given(generations.findById(GENERATION_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> facade.attendanceBoard(GENERATION_ID))
                .isInstanceOf(DataNotFoundException.class);
    }

    private void givenGeneration() {
        final Generation generation = mock(Generation.class);
        given(generation.getId()).willReturn(GENERATION_ID);
        given(generation.getName()).willReturn("13기");
        given(generations.findById(GENERATION_ID)).willReturn(Optional.of(generation));
    }

    private Schedule schedule(final Long id, final LocalDate date) {
        final Schedule schedule = mock(Schedule.class);
        given(schedule.getId()).willReturn(id);
        given(schedule.getDate()).willReturn(date);
        return schedule;
    }

    private User member(final Long id, final String name, final Long teamId) {
        final User user = mock(User.class);
        given(user.getId()).willReturn(id);
        given(user.getName()).willReturn(name);
        given(user.getTeamId()).willReturn(teamId);
        given(user.getJob()).willReturn(JobRole.BACKEND);
        return user;
    }

    private Team team(final Long id, final String name) {
        final Team team = mock(Team.class);
        given(team.getId()).willReturn(id);
        given(team.getName()).willReturn(name);
        return team;
    }
}
