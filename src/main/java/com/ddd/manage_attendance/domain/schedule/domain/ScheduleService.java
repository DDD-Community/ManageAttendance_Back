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
    public List<Schedule> findAllSchedulesByGenerationId(final Long generationId) {
        return scheduleRepository.findAllByGenerationIdOrderByDateAsc(generationId);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllScheduleResponses(final Long generationId) {
        return ScheduleResponse.fromList(findAllSchedulesByGenerationId(generationId));
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleByDateAndGenerationId(
            final LocalDate date, final Long generationId) {
        return scheduleRepository
                .findByDateAndGenerationId(date, generationId)
                .orElseThrow(DataNotFoundException::new);
    }
}
