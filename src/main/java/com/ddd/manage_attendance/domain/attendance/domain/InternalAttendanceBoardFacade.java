package com.ddd.manage_attendance.domain.attendance.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.core.util.TimeProvider;
import com.ddd.manage_attendance.domain.attendance.api.dto.InternalAttendanceBoard;
import com.ddd.manage_attendance.domain.attendance.api.dto.InternalAttendanceCell;
import com.ddd.manage_attendance.domain.attendance.api.dto.InternalGenerationInfo;
import com.ddd.manage_attendance.domain.attendance.api.dto.InternalMemberItem;
import com.ddd.manage_attendance.domain.attendance.api.dto.InternalScheduleItem;
import com.ddd.manage_attendance.domain.auth.domain.UserRepository;
import com.ddd.manage_attendance.domain.auth.domain.UserRole;
import com.ddd.manage_attendance.domain.generation.domain.Generation;
import com.ddd.manage_attendance.domain.generation.domain.GenerationRepository;
import com.ddd.manage_attendance.domain.schedule.domain.Schedule;
import com.ddd.manage_attendance.domain.schedule.domain.ScheduleRepository;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalAttendanceBoardFacade {
    private final GenerationRepository generations;
    private final ScheduleRepository schedules;
    private final TeamRepository teams;
    private final UserRepository users;
    private final AttendanceRepository attendances;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public InternalAttendanceBoard attendanceBoard(final Long id) {
        final Generation generation =
                generations.findById(id).orElseThrow(DataNotFoundException::new);
        final List<Schedule> scheduleList = schedules.findAllByGenerationIdOrderByDateAsc(id);
        final Map<Long, String> teamNames =
                teams.findAllByGenerationId(id).stream()
                        .collect(Collectors.toMap(Team::getId, Team::getName));
        final List<InternalMemberItem> members =
                users.findAllByGenerationIdAndRole(id, UserRole.MEMBER).stream()
                        .map(
                                user ->
                                        new InternalMemberItem(
                                                user.getId(),
                                                user.getName(),
                                                user.getTeamId(),
                                                teamNames.get(user.getTeamId()),
                                                user.getJob().name()))
                        .sorted(
                                Comparator.comparing(
                                                InternalMemberItem::teamName,
                                                Comparator.nullsLast(String::compareTo))
                                        .thenComparing(
                                                InternalMemberItem::name,
                                                Comparator.nullsLast(String::compareTo))
                                        .thenComparing(InternalMemberItem::id))
                        .toList();
        final List<InternalScheduleItem> scheduleItems =
                scheduleList.stream()
                        .map(
                                schedule ->
                                        new InternalScheduleItem(
                                                schedule.getId(),
                                                schedule.getName(),
                                                schedule.getDate(),
                                                schedule.getScheduleTime()))
                        .toList();
        final Map<String, AttendanceStatus> statuses = loadStatuses(scheduleList);
        final LocalDate today = timeProvider.nowDate();
        final List<InternalAttendanceCell> cells =
                buildCells(members, scheduleList, statuses, today);
        return new InternalAttendanceBoard(
                new InternalGenerationInfo(generation.getId(), generation.getName()),
                today,
                scheduleItems,
                members,
                cells);
    }

    private Map<String, AttendanceStatus> loadStatuses(final List<Schedule> scheduleList) {
        final List<Long> scheduleIds = scheduleList.stream().map(Schedule::getId).toList();
        if (scheduleIds.isEmpty()) {
            return Map.of();
        }
        return attendances.findAllByScheduleIdIn(scheduleIds).stream()
                .collect(
                        Collectors.toMap(
                                attendance ->
                                        key(attendance.getUserId(), attendance.getScheduleId()),
                                Attendance::getStatus));
    }

    private List<InternalAttendanceCell> buildCells(
            final List<InternalMemberItem> members,
            final List<Schedule> scheduleList,
            final Map<String, AttendanceStatus> statuses,
            final LocalDate today) {
        final List<InternalAttendanceCell> cells = new ArrayList<>();
        for (InternalMemberItem member : members) {
            for (Schedule schedule : scheduleList) {
                final AttendanceStatus status =
                        statuses.getOrDefault(
                                key(member.id(), schedule.getId()),
                                schedule.getDate().isBefore(today)
                                        ? AttendanceStatus.ABSENT
                                        : AttendanceStatus.NONE);
                cells.add(new InternalAttendanceCell(member.id(), schedule.getId(), status));
            }
        }
        return cells;
    }

    private String key(final Long userId, final Long scheduleId) {
        return userId + ":" + scheduleId;
    }
}
