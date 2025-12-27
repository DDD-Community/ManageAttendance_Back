package com.ddd.manage_attendance.domain.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ManagerRole {
    TEAM_MANAGING("팀매니징"),
    SCHEDULE_REMINDER("일정 리마인드"),
    PHOTO("사진 촬영"),
    LOCATION_RENTAL("장소 대관"),
    SNS_MANAGEMENT("SNS 관리"),
    ATTENDANCE_CHECK("출석 체크");

    private final String description;
}
