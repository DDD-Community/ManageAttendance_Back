package com.ddd.manage_attendance.domain.attendance.domain;

import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserIdAndCreatedDateBetween(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<Attendance> findByUserIdAndScheduleIdIn(Long userId, List<Long> scheduleIds);

    List<Attendance> findByScheduleIdAndUserIdIn(Long scheduleId, List<Long> userIds);

    Optional<Attendance> findByUserIdAndScheduleId(Long userId, Long scheduleId);

    @Query(
            """
            SELECT new com.ddd.manage_attendance.domain.attendance.domain.AttendanceSummary(
                COALESCE(SUM(CASE WHEN a.status = 'ATTENDED' THEN 1L ELSE 0L END), 0L),

                COALESCE(SUM(
                    CASE
                        WHEN s.date < CURRENT_DATE THEN
                            CASE
                                WHEN a.id IS NULL OR a.status = 'ABSENT' THEN 1L ELSE 0L
                            END
                        ELSE
                            CASE
                                WHEN a.status = 'ABSENT' THEN 1L ELSE 0L
                            END
                    END
                ), 0L),

                COALESCE(SUM(CASE WHEN a.status = 'LATE' THEN 1L ELSE 0L END), 0L)
            )
            FROM Schedule s
            LEFT JOIN Attendance a
                ON a.scheduleId = s.id
               AND a.userId = :userId
            WHERE s.generationId = :generationId
""")
    @QueryHints(
            @QueryHint(
                    name = "org.hibernate.comment",
                    value =
                            "AttendanceRepository.findStatusSummaryByUserIdAndGenerationId: 유저의 현재 기수 출석 현황 조회"))
    AttendanceSummary findStatusSummaryByUserIdAndGenerationId(
            @Param("userId") Long userId, @Param("generationId") Long generationId);

    @Query(
            """
                SELECT new com.ddd.manage_attendance.domain.attendance.domain.AttendanceSummary(
                    COALESCE(SUM(CASE WHEN a.status = 'ATTENDED' THEN 1L ELSE 0L END), 0L),

                    COALESCE(SUM(
                        CASE
                            WHEN s.date < CURRENT_DATE THEN
                                CASE
                                    WHEN a.status = 'ABSENT' OR a.id IS NULL THEN 1L
                                    ELSE 0L
                                END
                            ELSE
                                CASE
                                    WHEN a.status = 'ABSENT' THEN 1L
                                    ELSE 0L
                                END
                        END
                    ), 0L),

                    COALESCE(SUM(CASE WHEN a.status = 'LATE' THEN 1L ELSE 0L END), 0L)
                )
                FROM Schedule s
                JOIN User u
                    ON u.generationId = s.generationId
                LEFT JOIN Attendance a
                    ON a.scheduleId = s.id
                   AND a.userId = u.id
                WHERE s.id = :scheduleId
                GROUP BY s.id
""")
    @QueryHints(
            @QueryHint(
                    name = "org.hibernate.comment",
                    value = "AttendanceRepository.findStatusSummaryByGenerationId: 현재 기수 출석 현황 조회"))
    AttendanceSummary findStatusSummaryByScheduleId(@Param("scheduleId") Long scheduleId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Attendance a WHERE a.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
