package com.ddd.manage_attendance.domain.schedule.api;

import com.ddd.manage_attendance.domain.schedule.api.dto.ScheduleResponse;
import com.ddd.manage_attendance.domain.schedule.domain.ScheduleFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "세션 스케줄 조회 API - 모든 API JWT 필요")
@SecurityRequirement(name = "JWT")
public class ScheduleController {
    private final ScheduleFacade scheduleFacade;

    @GetMapping
    @Operation(summary = "[인증] 전체 스케줄 조회", description = "사용자가 속한 기수의 전체 스케줄을 조회합니다.")
    public List<ScheduleResponse> getAllSchedule(@AuthenticationPrincipal Long userId) {
        return scheduleFacade.getAllScheduleResponses(userId);
    }
}
