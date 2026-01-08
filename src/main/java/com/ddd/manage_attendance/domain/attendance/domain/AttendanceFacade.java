package com.ddd.manage_attendance.domain.attendance.domain;

import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceCheckInRequest;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceStatusModifyRequest;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceSummaryResponse;
import com.ddd.manage_attendance.domain.attendance.api.dto.TeamAttendancesResponse;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import com.ddd.manage_attendance.domain.qr.domain.QrService;
import com.ddd.manage_attendance.domain.schedule.api.dto.ScheduleWithAttendanceResponse;
import com.ddd.manage_attendance.domain.schedule.domain.Schedule;
import com.ddd.manage_attendance.domain.schedule.domain.ScheduleService;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceFacade {
    private final AttendanceService attendanceService;
    private final UserService userService;
    private final ScheduleService scheduleService;
    private final QrService qrService;
    private final TeamService teamService;

    @Transactional
    public void checkInByQrCode(final Long userId, final AttendanceCheckInRequest request) {
        // 운영진 권한 체크
        final User manager = userService.getUser(userId);
        manager.validateManager();

        final String qrCode = request.qrCode();
        final User user = userService.getUserByQrCode(qrCode);
        final LocalDate today = LocalDate.now();
        final Schedule schedule =
                scheduleService.getScheduleByDateAndGenerationId(today, user.getGenerationId());
        qrService.extractTokenIfValid(qrCode);
        attendanceService.checkInByQrCode(
                user.getId(), schedule.getId(), schedule.getScheduleTime(), today);
    }

    @Transactional(readOnly = true)
    public List<ScheduleWithAttendanceResponse> getAllMySchedules(final Long userId) {
        final User user = userService.getUser(userId);
        final List<Schedule> schedules =
                scheduleService.findAllSchedulesByGenerationId(user.getGenerationId());
        final List<Long> scheduleIds = schedules.stream().map(Schedule::getId).toList();
        final List<Attendance> attendances =
                attendanceService.findAllUserAttendancesByScheduleIds(userId, scheduleIds);
        final AttendanceStatusByScheduleIndex attendanceStatusByScheduleIndex =
                AttendanceStatusByScheduleIndex.from(attendances);
        return ScheduleWithAttendanceResponse.fromList(schedules, attendanceStatusByScheduleIndex);
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getMyAttendanceSummary(final Long userId) {
        final User user = userService.getUser(userId);
        return attendanceService.getAttendanceSummary(userId, user.getGenerationId());
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getGenerationAttendanceSummaryByScheduleId(
            final Long userId, final Long scheduleId) {

        final User user = userService.getUser(userId);
        user.validateManager();
        return attendanceService.getGenerationAttendanceSummaryByScheduleId(scheduleId);
    }

    @Transactional(readOnly = true)
    public List<TeamAttendancesResponse> getTeamAttendancesByScheduleId(
            final Long userId, final Long scheduleId, final Long teamId) {
        final User user = userService.getUser(userId);
        user.validateManager();

        final Team team = teamService.findById(teamId);
        final List<User> teamUsers = userService.findUsersByTeamId(teamId);

        final List<Long> userIds = teamUsers.stream().map(User::getId).toList();

        final List<Attendance> attendances =
                attendanceService.findAllUsersAttendancesByScheduleId(userIds, scheduleId);

        final AttendanceStatusByUserIndex statusIndex =
                AttendanceStatusByUserIndex.from(attendances);
        final AttendanceByUserIndex attendanceIndex = AttendanceByUserIndex.from(attendances);

        return TeamAttendancesResponse.fromList(
                teamUsers, team.getName(), statusIndex, attendanceIndex);
    }

    @Transactional
    public void modifyAttendance(
            final Long userId,
            final Long attendanceId,
            final AttendanceStatusModifyRequest request) {
        final User manager = userService.getUser(userId);
        manager.validateManager();

        final Attendance attendance = attendanceService.findAttendanceById(attendanceId);
        if (!Objects.equals(attendance.getUserId(), request.userId())) {
            throw new NotUserAttendanceException();
        }
        attendance.modifyStatus(request.status());
    }
}
