package com.ddd.manage_attendance.domain.schedule.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record InternalScheduleSyncRequest(
        @NotNull(message = "일정 목록은 필수입니다.")
                @Size(max = 30, message = "일정은 최대 30개까지 등록할 수 있습니다.")
                @Valid
                @Schema(description = "기수에 반영할 일정 목록")
                List<InternalScheduleSyncItem> schedules) {}
