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

/**
 * 팀 투표 선택 1건(부문별 선택 팀). {@code (response, category, team)} 단위의 정규화 행으로, 부문별 득표 집계가 순수 SQL 로 가능하다.
 * 부문별 작성 사유는 {@link TeamVoteReason} 에 별도 보관한다.
 */
@Getter
@Entity
@Table(name = "team_vote_answer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamVoteAnswer extends BaseEntity {

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

    @NotNull
    @Comment("선택한 팀 Id")
    @Column(name = "team_id", nullable = false, columnDefinition = "bigint")
    private Long teamId;

    @Builder(access = AccessLevel.PRIVATE)
    public TeamVoteAnswer(Long responseId, String categoryId, Long teamId) {
        this.responseId = responseId;
        this.categoryId = categoryId;
        this.teamId = teamId;
    }

    public static TeamVoteAnswer create(
            final Long responseId, final String categoryId, final Long teamId) {
        return TeamVoteAnswer.builder()
                .responseId(responseId)
                .categoryId(categoryId)
                .teamId(teamId)
                .build();
    }
}
