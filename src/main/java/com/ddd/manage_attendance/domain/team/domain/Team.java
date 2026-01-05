package com.ddd.manage_attendance.domain.team.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "team",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "UK_team_name_generation_id",
                    columnNames = {"name", "generation_id"})
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @NotNull
    @Comment("이름")
    @Column(name = "name", columnDefinition = "varchar(30)")
    private String name;

    @NotNull
    @Comment("기수 Id")
    @Column(name = "generation_id", columnDefinition = "bigint")
    private Long generationId;

    @Builder(access = AccessLevel.PRIVATE)
    public Team(String name, Long generationId) {
        this.name = name;
        this.generationId = generationId;
    }

    public static Team createTeam(String name, Long generationId) {
        return Team.builder().name(name).generationId(generationId).build();
    }
}
