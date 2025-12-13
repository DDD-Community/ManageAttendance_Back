package com.ddd.manage_attendance.domain.attendance.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceStatusByUserIndex {

    private final Map<Long, AttendanceStatus> byUserId;

    private AttendanceStatusByUserIndex(Map<Long, AttendanceStatus> byUserId) {
        this.byUserId = byUserId;
    }

    public static AttendanceStatusByUserIndex from(List<Attendance> attendances) {
        Map<Long, AttendanceStatus> map = new HashMap<>();
        for (Attendance attendance : attendances) {
            map.put(attendance.getUserId(), attendance.getStatus());
        }
        return new AttendanceStatusByUserIndex(map);
    }

    public AttendanceStatus getOrDefault(final Long userId, final AttendanceStatus defaultStatus) {
        return byUserId.getOrDefault(userId, defaultStatus);
    }
}
