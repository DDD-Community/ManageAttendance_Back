package com.ddd.manage_attendance.domain.schedule.api.dto;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatusByScheduleIndex;
import com.ddd.manage_attendance.domain.schedule.domain.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;

@Schema(title = "[스케줄] 스케줄/출석 현황 조회 응답 DTO")
public record ScheduleWithAttendanceResponse(
        @Schema(description = "스케줄 고유 ID", example = "1") Long id,
        @Schema(description = "제목", example = "제목입니다.") String name,
        @Schema(description = "출석 상태", example = "LATE") AttendanceStatus status,
        @Schema(description = "내용", example = "내용입니다.") String desc,
        @Schema(description = "월", example = "12") int month,
        @Schema(description = "요일", example = "25") int day) {
    public static ScheduleWithAttendanceResponse from(
            final Schedule schedule,
            final AttendanceStatusByScheduleIndex attendanceStatusByScheduleIndex) {
        return new ScheduleWithAttendanceResponse(
                schedule.getId(),
                schedule.getName(),
                attendanceStatusByScheduleIndex.getOrDefault(
                        schedule.getId(), AttendanceStatus.NONE),
                schedule.getDescription(),
                schedule.getDate().getMonthValue(),
                schedule.getDate().getDayOfMonth());
    }

    public static List<ScheduleWithAttendanceResponse> fromList(
            final List<Schedule> schedules,
            final AttendanceStatusByScheduleIndex attendanceStatusByScheduleIndex) {
        return schedules.stream()
                .map(s -> from(s, attendanceStatusByScheduleIndex))
                .collect(Collectors.toList());
    }
}
