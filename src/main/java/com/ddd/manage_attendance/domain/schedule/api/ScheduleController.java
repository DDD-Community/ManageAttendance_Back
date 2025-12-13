package com.ddd.manage_attendance.domain.schedule.api;

import com.ddd.manage_attendance.domain.schedule.api.dto.ScheduleResponse;
import com.ddd.manage_attendance.domain.schedule.domain.ScheduleFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "스케줄 API", description = "스케줄 API 입니다.")
public class ScheduleController {
    private final ScheduleFacade scheduleFacade;

    @GetMapping
    @Operation(summary = "전체 스케줄 조회", description = "전체 스케줄을 조회 합니다.")
    public List<ScheduleResponse> getAllSchedule(Long userId) {
        return scheduleFacade.getAllScheduleResponses(userId);
    }
}
