package com.ddd.manage_attendance.domain.attendance.domain;

import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceCheckInRequest;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import com.ddd.manage_attendance.domain.qr.domain.QrService;
import com.ddd.manage_attendance.domain.schedule.domain.Schedule;
import com.ddd.manage_attendance.domain.schedule.domain.ScheduleService;
import java.time.LocalDate;
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

    @Transactional
    public void checkInByQrCode(final AttendanceCheckInRequest request) {
        final String qrCode = request.qrCode();
        qrService.extractTokenIfValid(qrCode);
        final User user = userService.getUserByQrCode(qrCode);
        final LocalDate today = LocalDate.now();
        final Schedule schedule = scheduleService.getScheduleByDate(today);
        attendanceService.checkInByQrCode(
                user.getId(), schedule.getId(), schedule.getScheduleTime(), today);
    }
}
