package com.ddd.manage_attendance.domain.schedule.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.domain.schedule.api.dto.ScheduleResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<Schedule> findAllSchedules() {
        return scheduleRepository.findAllByOrderByDateAsc();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllScheduleResponses() {
        return ScheduleResponse.fromList(findAllSchedules());
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleByDate(final LocalDate date) {
        return scheduleRepository.findByDate(date).orElseThrow(DataNotFoundException::new);
    }
}
