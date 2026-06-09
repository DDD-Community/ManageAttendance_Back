package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/** 팀 투표 부문별 작성 사유(부문당 1건). */
@Getter
@Entity
@Table(name = "team_vote_reason")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamVoteReason extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @NotNull
    @Comment("응답 Id")
    @Column(name = "response_id", nullable = false, columnDefinition = "bigint")
    private Long responseId;

    @NotNull
    @Comment("부문 Id (템플릿의 안정 시맨틱 Id)")
    @Column(name = "category_id", nullable = false, columnDefinition = "varchar(100)")
    private String categoryId;

    @Comment("작성 사유")
    @Column(name = "reason", columnDefinition = "varchar(500)")
    private String reason;

    @Builder(access = AccessLevel.PRIVATE)
    public TeamVoteReason(Long responseId, String categoryId, String reason) {
        this.responseId = responseId;
        this.categoryId = categoryId;
        this.reason = reason;
    }

    public static TeamVoteReason create(
            final Long responseId, final String categoryId, final String reason) {
        return TeamVoteReason.builder()
                .responseId(responseId)
                .categoryId(categoryId)
                .reason(reason)
                .build();
    }
}
