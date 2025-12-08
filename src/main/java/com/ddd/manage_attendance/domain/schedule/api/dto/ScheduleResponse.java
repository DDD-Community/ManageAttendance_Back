package com.ddd.manage_attendance.domain.schedule.api.dto;

import com.ddd.manage_attendance.domain.schedule.domain.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;

@Schema(title = "[스케줄] 스케줄 조회 응답 DTO")
public record ScheduleResponse(
        @Schema(description = "스케줄 고유 ID", example = "1") Long id,
        @Schema(description = "제목", example = "제목입니다.") String name,
        @Schema(description = "내용", example = "내용입니다.") String desc,
        @Schema(description = "월", example = "12") int month,
        @Schema(description = "요일", example = "25") int day) {
    public static ScheduleResponse from(final Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getName(),
                schedule.getDesc(),
                schedule.getDate().getMonthValue(),
                schedule.getDate().getDayOfMonth());
    }

    public static List<ScheduleResponse> fromList(final List<Schedule> schedules) {
        return schedules.stream().map(ScheduleResponse::from).collect(Collectors.toList());
    }
}
