package com.ddd.manage_attendance.domain.schedule.domain;

import jakarta.persistence.QueryHint;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByOrderByDateAsc();

    Optional<Schedule> findByDate(LocalDate today);

    @Query(
            """
    SELECT new com.ddd.manage_attendance.domain.schedule.domain.ScheduleWithAttendanceProjection(
        s.id, a.status,s.name,s.desc,s.date
    )
    FROM Schedule s
    JOIN Attendance a ON s.id = a.scheduleId
    WHERE a.userId = :userId
""")
    @QueryHints(
            @QueryHint(
                    name = "org.hibernate.comment",
                    value =
                            "ScheduleRepository.findAllWithAttendanceProjection : 세션 별, 유저의 출석 현황을 조회 합니다."))
    List<ScheduleWithAttendanceProjection> findAllWithAttendanceProjection(
            @Param("userId") Long userId);
}
