package com.ddd.manage_attendance.domain.attendance.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@Entity
@Table(
        name = "attendance",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_attendance_schedule_member",
                    columnNames = {"schedule_id", "member_id"})
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @NotNull
    @Comment("유저 Id")
    @Column(name = "member_id", columnDefinition = "bigint")
    private Long userId;

    @NotNull
    @Comment("스케줄 Id")
    @Column(name = "schedule_id", columnDefinition = "bigint")
    private Long scheduleId;

    @NotNull
    @Comment("상태 값")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(10)")
    private AttendanceStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    public Attendance(Long userId, Long scheduleId, AttendanceStatus status) {
        this.userId = userId;
        this.scheduleId = scheduleId;
        this.status = status;
    }

    public static Attendance create(Long userId, Long scheduleId, AttendanceStatus status) {
        return Attendance.builder().userId(userId).scheduleId(scheduleId).status(status).build();
    }

    public void modifyStatus(AttendanceStatus status) {
        this.status = status;
    }
}
