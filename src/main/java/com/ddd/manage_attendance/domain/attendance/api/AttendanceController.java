package com.ddd.manage_attendance.domain.attendance.api;

import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceCheckInRequest;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name = "출석 API", description = "출석 API 입니다.")
public class AttendanceController {

    private final AttendanceFacade attendanceFacade;

    @PostMapping
    @Operation(summary = "출석", description = "출석을 합니다.")
    public void checkIn(@RequestBody final AttendanceCheckInRequest request) {
        attendanceFacade.checkInByQrCode(request);
    }
}
