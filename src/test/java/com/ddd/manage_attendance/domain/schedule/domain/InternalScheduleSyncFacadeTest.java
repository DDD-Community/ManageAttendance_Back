package com.ddd.manage_attendance.domain.schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.core.exception.ErrorCode;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceRepository;
import com.ddd.manage_attendance.domain.generation.domain.GenerationRepository;
import com.ddd.manage_attendance.domain.schedule.api.dto.InternalScheduleSkip;
import com.ddd.manage_attendance.domain.schedule.api.dto.InternalScheduleSyncItem;
import com.ddd.manage_attendance.domain.schedule.api.dto.InternalScheduleSyncResult;
import com.ddd.manage_attendance.domain.schedule.exception.ScheduleDuplicatedDateException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalScheduleSyncFacadeTest {
    private static final Long GENERATION_ID = 13L;
    private static final LocalDate REQUEST_DATE = LocalDate.of(2026, 11, 7);

    @Mock private ScheduleRepository schedules;
    @Mock private AttendanceRepository attendances;
    @Mock private GenerationRepository generations;
    @InjectMocks private InternalScheduleSyncFacade facade;

    @Test
    @DisplayName("새 일정을 생성하고 설명이 비면 이름으로 채운다")
    void syncSchedules_whenNewSchedule_createsWithNameAsDescription() {
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        final InternalScheduleSyncItem item = item(REQUEST_DATE, "오리엔테이션", " ");

        final InternalScheduleSyncResult result =
                facade.syncSchedules(GENERATION_ID, List.of(item));

        assertThat(result.created()).containsExactly(REQUEST_DATE);
        verify(schedules)
                .saveAndFlush(
                        org.mockito.ArgumentMatchers.argThat(
                                schedule ->
                                        schedule.getName().equals("오리엔테이션")
                                                && schedule.getDescription().equals("오리엔테이션")));
    }

    @Test
    @DisplayName("기존 일정 내용이 바뀌면 수정한다")
    void syncSchedules_whenValuesDiffer_updates() {
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        final Schedule schedule = existingSchedule(REQUEST_DATE, "기존", "기존 설명", LocalTime.NOON);
        given(schedules.findAllByGenerationIdOrderByDateAsc(GENERATION_ID))
                .willReturn(List.of(schedule));

        final InternalScheduleSyncResult result =
                facade.syncSchedules(GENERATION_ID, List.of(item(REQUEST_DATE, "변경", "설명")));

        assertThat(result.updated()).containsExactly(REQUEST_DATE);
        verify(schedule).update("변경", "설명", LocalTime.of(14, 0));
    }

    @Test
    @DisplayName("같은 일정은 변경하지 않는다")
    void syncSchedules_whenValuesSame_keepsUnchanged() {
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        final Schedule schedule = existingSchedule(REQUEST_DATE, "일정", "설명", LocalTime.of(14, 0));
        given(schedules.findAllByGenerationIdOrderByDateAsc(GENERATION_ID))
                .willReturn(List.of(schedule));

        final InternalScheduleSyncResult result =
                facade.syncSchedules(GENERATION_ID, List.of(item(REQUEST_DATE, "일정", "설명")));

        assertThat(result.unchanged()).containsExactly(REQUEST_DATE);
        verify(schedule, never()).update(any(), any(), any());
    }

    @Test
    @DisplayName("요청에 없고 출석 기록도 없는 일정은 삭제한다")
    void syncSchedules_whenRemovedScheduleHasNoAttendance_deletes() {
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        final Schedule schedule = existingSchedule(REQUEST_DATE, "일정", "설명", LocalTime.NOON);
        given(schedules.findAllByGenerationIdOrderByDateAsc(GENERATION_ID))
                .willReturn(List.of(schedule));

        final InternalScheduleSyncResult result = facade.syncSchedules(GENERATION_ID, List.of());

        assertThat(result.deleted()).containsExactly(REQUEST_DATE);
        verify(schedules).delete(schedule);
    }

    @Test
    @DisplayName("요청에서 빠진 일정에 출석 기록이 있으면 유지한다")
    void syncSchedules_whenRemovedScheduleHasAttendance_keeps() {
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        final Schedule schedule = existingSchedule(REQUEST_DATE, "일정", "설명", LocalTime.NOON);
        given(schedules.findAllByGenerationIdOrderByDateAsc(GENERATION_ID))
                .willReturn(List.of(schedule));
        given(attendances.existsByScheduleId(7L)).willReturn(true);

        final InternalScheduleSyncResult result = facade.syncSchedules(GENERATION_ID, List.of());

        assertThat(result.kept())
                .containsExactly(new InternalScheduleSkip(REQUEST_DATE, "HAS_ATTENDANCE"));
        verify(schedules, never()).delete(schedule);
    }

    @Test
    @DisplayName("다른 기수가 사용하는 날짜는 건너뛴다")
    void syncSchedules_whenDateTaken_skips() {
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        given(schedules.existsByDate(REQUEST_DATE)).willReturn(true);

        final InternalScheduleSyncResult result =
                facade.syncSchedules(GENERATION_ID, List.of(item(REQUEST_DATE, "일정", "설명")));

        assertThat(result.skipped())
                .containsExactly(
                        new InternalScheduleSkip(REQUEST_DATE, "DATE_TAKEN_BY_OTHER_GENERATION"));
        verify(schedules, never()).saveAndFlush(any(Schedule.class));
    }

    @Test
    @DisplayName("요청 날짜가 중복되면 400 오류를 반환한다")
    void syncSchedules_whenDuplicateDates_throwsBadRequest() {
        given(generations.existsById(GENERATION_ID)).willReturn(true);

        assertThatThrownBy(
                        () ->
                                facade.syncSchedules(
                                        GENERATION_ID,
                                        List.of(
                                                item(REQUEST_DATE, "일정", "설명"),
                                                item(REQUEST_DATE, "다른 일정", "설명"))))
                .isInstanceOf(ScheduleDuplicatedDateException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCHEDULE_DUPLICATED_DATE);
        verify(schedules, never()).saveAndFlush(any(Schedule.class));
    }

    @Test
    @DisplayName("존재하지 않는 기수는 동기화할 수 없다")
    void syncSchedules_whenGenerationMissing_throwsNotFound() {
        assertThatThrownBy(() -> facade.syncSchedules(GENERATION_ID, List.of()))
                .isInstanceOf(DataNotFoundException.class);
    }

    private InternalScheduleSyncItem item(LocalDate date, String name, String description) {
        return new InternalScheduleSyncItem(date, LocalTime.of(14, 0), name, description);
    }

    private Schedule existingSchedule(
            LocalDate date, String name, String description, LocalTime scheduleTime) {
        // 테스트마다 읽는 필드가 달라 공용 헬퍼의 stub 은 lenient 로 둔다
        final Schedule schedule = mock(Schedule.class);
        lenient().when(schedule.getDate()).thenReturn(date);
        lenient().when(schedule.getId()).thenReturn(7L);
        lenient().when(schedule.getName()).thenReturn(name);
        lenient().when(schedule.getDescription()).thenReturn(description);
        lenient().when(schedule.getScheduleTime()).thenReturn(scheduleTime);
        return schedule;
    }
}
