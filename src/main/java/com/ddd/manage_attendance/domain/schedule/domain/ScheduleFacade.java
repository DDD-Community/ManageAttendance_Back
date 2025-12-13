package com.ddd.manage_attendance.domain.schedule.domain;

import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import com.ddd.manage_attendance.domain.schedule.api.dto.ScheduleResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleFacade {
    private final ScheduleService scheduleService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllScheduleResponses(final Long userId) {
        final User user = userService.getUser(userId);
        return scheduleService.getAllScheduleResponses(user.getGenerationId());
    }
}
