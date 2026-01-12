package com.ddd.manage_attendance.domain.attendance.api;

import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceCheckInRequest;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceCheckInResponse;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceStatusModifyRequest;
import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceStatusResponse;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceFacade;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Attendance", description = "출석 체크 및 관리 API")
public class AttendanceController {

    private final AttendanceFacade attendanceFacade;
    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(
            summary = "[운영진] 출석 체크",
            description =
                    "QR 코드를 스캔하여 사용자의 출석을 체크합니다.\n\n" + "- 운영진 권한 필수\n" + "- QR 코드 검증 후 출석 처리")
    @SecurityRequirement(name = "JWT")
    public AttendanceCheckInResponse checkIn(
            @AuthenticationPrincipal final Long userId,
            @RequestBody final AttendanceCheckInRequest request) {
        return attendanceFacade.checkInByQrCode(userId, request);
    }

    @GetMapping("/status")
    @Operation(
            summary = "[공개] 출석 상태 코드 목록 조회",
            description = "출석 상태 코드 목록을 조회합니다. (출석, 지각, 결석 등)\n\n" + "- 인증 불필요")
    public List<AttendanceStatusResponse> getStatus() {
        return attendanceService.getStatus();
    }

    @PutMapping("/{attendanceId}")
    @Operation(
            summary = "[운영진] 출석 상태 변경",
            description =
                    "출석 ID로 출석 상태를 변경합니다.\n\n" + "- 운영진 권한 필수\n" + "- 출석 → 지각, 결석 → 출석 등 변경 가능")
    @SecurityRequirement(name = "JWT")
    public void modifyAttendance(
            @AuthenticationPrincipal final Long userId,
            @PathVariable @Positive Long attendanceId,
            @RequestBody final AttendanceStatusModifyRequest request) {
        attendanceFacade.modifyAttendance(userId, attendanceId, request);
    }
}
