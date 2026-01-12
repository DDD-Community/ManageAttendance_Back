package com.ddd.manage_attendance.domain.attendance.api;

import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceCheckInRequest;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceCheckInResponse;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceStatusModifyRequest;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceStatusResponse;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceFacade;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name = "출석 API", description = "출석 API 입니다.")
public class AttendanceController {

    private final AttendanceFacade attendanceFacade;
    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "출석", description = "출석을 합니다.")
    public AttendanceCheckInResponse checkIn(
            @AuthenticationPrincipal final Long userId,
            @RequestBody final AttendanceCheckInRequest request) {
        return attendanceFacade.checkInByQrCode(userId, request);
    }

    @GetMapping("/status")
    @Operation(summary = "출석 상태 값 조회", description = "출석 상태 값을 조회 합니다.")
    public List<AttendanceStatusResponse> getStatus() {
        return attendanceService.getStatus();
    }

    @PutMapping("/{attendanceId}")
    @Operation(summary = "출석 변경", description = "출석을 변경 합니다.")
    public void modifyAttendance(
            @AuthenticationPrincipal final Long userId,
            @PathVariable @Positive Long attendanceId,
            @RequestBody final AttendanceStatusModifyRequest request) {
        attendanceFacade.modifyAttendance(userId, attendanceId, request);
    }
}
