package com.ddd.manage_attendance.domain.schedule.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record InternalScheduleSyncResult(
        @Schema(description = "생성한 날짜") List<LocalDate> created,
        @Schema(description = "수정한 날짜") List<LocalDate> updated,
        @Schema(description = "변경하지 않은 날짜") List<LocalDate> unchanged,
        @Schema(description = "삭제한 날짜") List<LocalDate> deleted,
        @Schema(description = "출석 기록으로 유지한 일정") List<InternalScheduleSkip> kept,
        @Schema(description = "다른 기수 날짜와 충돌해 건너뛴 일정") List<InternalScheduleSkip> skipped) {}
