package com.ddd.manage_attendance.domain.attendance.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceStatusModifyRequest;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceStatusResponse;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceSummaryResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.ddd.manage_attendance.domain.auth.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private static final long attendLimitTime = 10;
    private static final long lateLimitTime = 30;

    @Transactional
    public AttendanceStatus checkInByQrCode(
            final Long userId,
            final Long scheduleId,
            final LocalTime scheduleTime,
            final LocalDate date) {
        final LocalDateTime start = date.atStartOfDay();
        final LocalDateTime end = date.plusDays(1).atStartOfDay();

        final LocalTime now = LocalTime.now();
        final AttendanceStatus status = decideStatus(scheduleTime, now);

        attendanceRepository
                .findByUserIdAndCreatedDateBetween(userId, start, end)
                .ifPresent(
                        attendance -> {
                            throw new DuplicatedAttendanceException();
                        });

        saveAttendance(userId, scheduleId, status);

        return status;
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getAttendanceSummary(
            final Long userId, final Long generationId) {
        final AttendanceSummary attendanceSummary =
                attendanceRepository.findStatusSummaryByUserIdAndGenerationId(userId, generationId);
        return AttendanceSummaryResponse.from(attendanceSummary);
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getGenerationAttendanceSummaryByScheduleId(
            final Long scheduleId) {
        final AttendanceSummary attendanceSummary =
                attendanceRepository.findStatusSummaryByScheduleId(scheduleId, UserRole.MANAGER);
        return AttendanceSummaryResponse.from(attendanceSummary);
    }

    @Transactional(readOnly = true)
    public List<Attendance> findAllUserAttendancesByScheduleIds(
            final Long userId, final List<Long> scheduleIds) {
        return attendanceRepository.findByUserIdAndScheduleIdIn(userId, scheduleIds);
    }

    @Transactional(readOnly = true)
    public List<Attendance> findAllUsersAttendancesByScheduleId(
            final List<Long> userIds, final Long scheduleId) {
        return attendanceRepository.findByScheduleIdAndUserIdIn(scheduleId, userIds);
    }

    @Transactional
    public void saveAttendance(
            final Long userId, final Long scheduleId, final AttendanceStatus status) {
        attendanceRepository.save(Attendance.create(userId, scheduleId, status));
    }

    @Transactional
    public void upsertAttendance(final AttendanceStatusModifyRequest request) {
        if (request.attendanceId() != null) {
            final Attendance attendance =
                    attendanceRepository
                            .findById(request.attendanceId())
                            .orElseThrow(DataNotFoundException::new);

            if (!Objects.equals(attendance.getUserId(), request.userId())) {
                throw new NotUserAttendanceException();
            }
            attendance.modifyStatus(request.status());
            return;
        }

        final Attendance attendance =
                attendanceRepository
                        .findByUserIdAndScheduleId(request.userId(), request.scheduleId())
                        .orElseGet(
                                () ->
                                        createAttendanceSafely(
                                                request.userId(),
                                                request.scheduleId(),
                                                request.status()));

        attendance.modifyStatus(request.status());
    }

    private Attendance createAttendanceSafely(
            final Long userId, final Long scheduleId, final AttendanceStatus status) {
        try {
            return attendanceRepository.save(Attendance.create(userId, scheduleId, status));
        } catch (DataIntegrityViolationException e) {
            return attendanceRepository
                    .findByUserIdAndScheduleId(userId, scheduleId)
                    .orElseThrow(() -> e);
        }
    }

    public List<AttendanceStatusResponse> getStatus() {
        return Arrays.stream(AttendanceStatus.values())
                .filter(status -> status != AttendanceStatus.NONE)
                .map(AttendanceStatusResponse::from)
                .toList();
    }

    private AttendanceStatus decideStatus(final LocalTime scheduleTime, final LocalTime now) {

        final LocalTime attendedLimit = scheduleTime.plusMinutes(attendLimitTime);
        final LocalTime lateLimit = scheduleTime.plusMinutes(lateLimitTime);

        if (!now.isAfter(attendedLimit)) {
            return AttendanceStatus.ATTENDED;
        }

        if (!now.isAfter(lateLimit)) {
            return AttendanceStatus.LATE;
        }

        return AttendanceStatus.ABSENT;
    }
}
