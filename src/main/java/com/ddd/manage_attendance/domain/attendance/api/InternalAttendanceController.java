package com.ddd.manage_attendance.domain.attendance.api;

import com.ddd.manage_attendance.domain.attendance.api.dto.InternalAttendanceBoard;
import com.ddd.manage_attendance.domain.attendance.domain.InternalAttendanceBoardFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/generations")
@RequiredArgsConstructor
@Tag(name = "내부 출석 API", description = "내부 시스템에서 기수별 출석 보드를 조회합니다.")
@SecurityRequirement(name = "InternalApiKey")
public class InternalAttendanceController {
    private final InternalAttendanceBoardFacade facade;

    @GetMapping("/{generationId}/attendance-board")
    @Operation(
            summary = "[내부] 기수별 출석 보드 조회",
            description = "회원과 일정별 출석 상태를 조회합니다. 기수가 없으면 404를 반환합니다.")
    public InternalAttendanceBoard board(@PathVariable @Positive final Long generationId) {
        return facade.attendanceBoard(generationId);
    }
}
