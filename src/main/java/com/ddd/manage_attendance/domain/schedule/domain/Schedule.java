package com.ddd.manage_attendance.domain.schedule.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "schedule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @NotNull
    @Comment("이름")
    @Column(name = "name", nullable = false, columnDefinition = "varchar(30)")
    private String name;

    @NotNull
    @Comment("설명")
    @Column(name = "description", nullable = false, columnDefinition = "varchar(100)")
    private String description;

    @NotNull
    @Comment("세션 날짜")
    @Column(unique = true, name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @Comment("세션 시간")
    @Column(name = "schedule_time", nullable = false)
    private LocalTime scheduleTime;

    @NotNull
    @Comment("기수 Id")
    @Column(name = "generation_id", columnDefinition = "bigint")
    private Long generationId;

    public AttendanceStatus statusByDate() {
        final LocalDate today = LocalDate.now();

        if (this.date.isBefore(today)) {
            return AttendanceStatus.ABSENT;
        }

        return AttendanceStatus.NONE;
    }
}
