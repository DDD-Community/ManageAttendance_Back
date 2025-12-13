package com.ddd.manage_attendance.domain.attendance.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserIdAndCreatedDateBetween(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<Attendance> findAttendancesByStatusAndUserId(AttendanceStatus status, Long userId);

    List<Attendance> findAttendancesByUserId(Long userId);
}
