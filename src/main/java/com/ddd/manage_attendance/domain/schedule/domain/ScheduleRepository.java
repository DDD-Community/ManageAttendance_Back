package com.ddd.manage_attendance.domain.schedule.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByOrderByDateAsc();
}
