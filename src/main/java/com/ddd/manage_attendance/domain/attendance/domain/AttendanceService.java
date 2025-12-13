package com.ddd.manage_attendance.domain.attendance.domain;

import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceSummaryResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private static final long attendLimitTime = 10;
    private static final long lateLimitTime = 30;

    @Transactional
    public void checkInByQrCode(
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

        attendanceRepository.save(Attendance.checkIn(userId, scheduleId, status));
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
                attendanceRepository.findStatusSummaryByScheduleId(scheduleId);
        return AttendanceSummaryResponse.from(attendanceSummary);
    }

    @Transactional(readOnly = true)
    public List<Attendance> findAllUserAttendancesByScheduleIds(
            final Long userId, final List<Long> scheduleIds) {
        return attendanceRepository.findByUserIdAndScheduleIdIn(userId, scheduleIds);
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
