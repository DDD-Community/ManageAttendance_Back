package com.ddd.manage_attendance.core.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class TimeProvider {
    private final Clock clock = Clock.system(ZoneId.of("Asia/Seoul"));

    public LocalTime nowTime() {
        return LocalTime.now(clock);
    }

    public LocalDate nowDate() {
        return LocalDate.now(clock);
    }

    public LocalDateTime nowDateTime() {
        return LocalDateTime.now(clock);
    }
}
