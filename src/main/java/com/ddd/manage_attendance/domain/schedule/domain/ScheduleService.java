package com.ddd.manage_attendance.domain.schedule.domain;

import com.ddd.manage_attendance.domain.schedule.api.dto.ScheduleResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
                .orElseThrow(NoScheduleException::new);
    }

    /** 해당 날짜의 일정을 조회한다(없을 수 있음). 일정이 없는 날을 정상 흐름으로 다룰 때 사용한다. */
    @Transactional(readOnly = true)
    public Optional<Schedule> findScheduleByDateAndGenerationId(
            final LocalDate date, final Long generationId) {
        return scheduleRepository.findByDateAndGenerationId(date, generationId);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getScheduleByGenerationIdBeforeDate(
            final LocalDate date, final Long generationId) {
        return scheduleRepository.findAllByGenerationIdAndDateBeforeOrderByDateAsc(
                generationId, date);
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleById(final Long scheduleId) {
        return scheduleRepository.findById(scheduleId).orElseThrow(NoScheduleException::new);
    }
}
