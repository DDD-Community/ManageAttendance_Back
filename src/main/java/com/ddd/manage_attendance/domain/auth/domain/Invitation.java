package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import com.ddd.manage_attendance.domain.generation.domain.Generation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(
        name = "invitation",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_invitation_code",
                    columnNames = {"code"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint")
    private Long id;

    @NotNull
    @Comment("초대 코드")
    @Column(name = "code", nullable = false, columnDefinition = "varchar(50)")
    private String code;

    @NotNull
    @Comment("초대 유형 (MEMBER/MANAGER)")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "varchar(20)")
    private InvitationType type;

    @NotNull
    @Comment("기수 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id", nullable = false, columnDefinition = "bigint")
    private Generation generation;

    @Comment("설명 (예: 13기 운영진용)")
    @Column(name = "description", columnDefinition = "varchar(255)")
    private String description;

    @Builder
    public Invitation(String code, InvitationType type, Generation generation, String description) {
        this.code = code;
        this.type = type;
        this.generation = generation;
        this.description = description;
    }
}
