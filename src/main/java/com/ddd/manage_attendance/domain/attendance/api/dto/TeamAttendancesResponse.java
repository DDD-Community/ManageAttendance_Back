package com.ddd.manage_attendance.domain.attendance.api.dto;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceByUserIndex;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatusByUserIndex;
import com.ddd.manage_attendance.domain.auth.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;

public record TeamAttendancesResponse(
        @Schema(description = "출석 id", example = "1L") Long attendanceId,
        @Schema(description = "팀원 id", example = "1L") Long userId,
        @Schema(description = "팀원 이름", example = "홍길동") String userName,
        @Schema(description = "팀원 정보", example = "Web1팀/BE") String userInfo,
        @Schema(description = "츨석 상태", example = "LATE") AttendanceStatus attendanceStatus) {

    private static final String TEAM_JOB_DELIMITER = "/";

    private static String userInfoOf(final String teamName, final User user) {
        return teamName + TEAM_JOB_DELIMITER + user.getJob().getDescription();
    }

    public static TeamAttendancesResponse from(
            final User user,
            final String teamName,
            final AttendanceStatusByUserIndex attendanceStatusByUserIndex,
            final AttendanceByUserIndex attendanceByUserIndex) {
        return new TeamAttendancesResponse(
                attendanceByUserIndex.get(user.getId()),
                user.getId(),
                user.getName(),
                userInfoOf(teamName, user),
                attendanceStatusByUserIndex.getOrDefault(user.getId(), AttendanceStatus.NONE));
    }

    public static List<TeamAttendancesResponse> fromList(
            final List<User> users,
            final String teamName,
            final AttendanceStatusByUserIndex attendanceStatusByUserIndex,
            final AttendanceByUserIndex attendanceByUserIndex) {
        return users.stream()
                .map(u -> from(u, teamName, attendanceStatusByUserIndex, attendanceByUserIndex))
                .collect(Collectors.toList());
    }
}
