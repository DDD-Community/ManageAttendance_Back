package com.ddd.manage_attendance.domain.attendance.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceByUserIndex {

    private final Map<Long, Long> byUserId;

    private AttendanceByUserIndex(Map<Long, Long> byScheduleId) {
        this.byUserId = byScheduleId;
    }

    public static AttendanceByUserIndex from(List<Attendance> attendances) {
        Map<Long, Long> map = new HashMap<>();
        for (Attendance attendance : attendances) {
            map.put(attendance.getScheduleId(), attendance.getId());
        }
        return new AttendanceByUserIndex(map);
    }

    public Long get(final Long userId) {
        return byUserId.get(userId);
    }
}
