package com.ddd.manage_attendance.domain.schedule.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByGenerationIdOrderByDateAsc(Long generationId);

    List<Schedule> findAllByGenerationIdAndDateBeforeOrderByDateAsc(
            Long generationId, LocalDate date);

    Optional<Schedule> findByDateAndGenerationId(LocalDate today, Long generationId);
}
