package com.ddd.manage_attendance.domain.schedule.api;

import com.ddd.manage_attendance.domain.schedule.api.dto.InternalScheduleItem;
import com.ddd.manage_attendance.domain.schedule.api.dto.InternalScheduleSyncRequest;
import com.ddd.manage_attendance.domain.schedule.api.dto.InternalScheduleSyncResult;
import com.ddd.manage_attendance.domain.schedule.domain.InternalScheduleSyncFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/generations/{generationId}/schedules")
@RequiredArgsConstructor
@Tag(name = "내부 일정 API", description = "내부 시스템에서 기수별 일정을 조회하고 동기화합니다.")
@SecurityRequirement(name = "InternalApiKey")
public class InternalScheduleController {
    private final InternalScheduleSyncFacade facade;

    @GetMapping
    @Operation(summary = "[내부] 기수 일정 조회", description = "기수의 일정을 날짜순으로 출석 기록 수와 함께 조회합니다.")
    public List<InternalScheduleItem> list(@PathVariable @Positive final Long generationId) {
        return facade.listSchedules(generationId);
    }

    @PutMapping
    @Operation(summary = "[내부] 기수 일정 동기화", description = "요청한 일정 목록과 기수의 기존 일정을 안전하게 동기화합니다.")
    public InternalScheduleSyncResult sync(
            @PathVariable @Positive final Long generationId,
            @Valid @RequestBody final InternalScheduleSyncRequest request) {
        return facade.syncSchedules(generationId, request.schedules());
    }
}
