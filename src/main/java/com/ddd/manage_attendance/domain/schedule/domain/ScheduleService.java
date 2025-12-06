package com.ddd.manage_attendance.domain.schedule.domain;

import com.ddd.manage_attendance.domain.schedule.api.dto.ScheduleResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllSchedule() {
        final List<Schedule> schedules = scheduleRepository.findAllByOrderByDateAsc();
        return ScheduleResponse.fromList(schedules);
    }
}
