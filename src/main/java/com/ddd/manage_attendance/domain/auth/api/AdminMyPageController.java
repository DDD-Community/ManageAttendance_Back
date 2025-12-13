package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceSummaryResponse;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/me")
@RequiredArgsConstructor
@Tag(name = "[운영진] 마이페이지 API", description = "운영진 마이페이지 API 입니다.")
public class AdminMyPageController {
    private final AttendanceFacade attendanceFacade;

    @GetMapping("/schedules/{scheduleId}/attendances")
    @Operation(summary = "기수 출석 현황 요약 조회", description = "기수 출석 현황 요약을 조회 합니다.")
    public AttendanceSummaryResponse getGenerationAttendanceSummaryByScheduleId(
            Long userId, @PathVariable @Positive final Long scheduleId) {
        return attendanceFacade.getGenerationAttendanceSummaryByScheduleId(userId, scheduleId);
    }
}
