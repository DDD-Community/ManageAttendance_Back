package com.ddd.manage_attendance.domain.attendance.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceStatusByScheduleIndex {

    private final Map<Long, AttendanceStatus> byScheduleId;

    private AttendanceStatusByScheduleIndex(Map<Long, AttendanceStatus> byScheduleId) {
        this.byScheduleId = byScheduleId;
    }

    public static AttendanceStatusByScheduleIndex from(List<Attendance> attendances) {
        Map<Long, AttendanceStatus> map = new HashMap<>();
        for (Attendance attendance : attendances) {
            map.put(attendance.getScheduleId(), attendance.getStatus());
        }
        return new AttendanceStatusByScheduleIndex(map);
    }

    public AttendanceStatus getOrDefault(
            final Long scheduleId, final AttendanceStatus defaultStatus) {
        return byScheduleId.getOrDefault(scheduleId, defaultStatus);
    }
}
