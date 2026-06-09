package com.ddd.manage_attendance.domain.vote.domain;

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

/**
 * 한 멤버의 투표 응답(투표당 1인 1응답). {@code (vote_id, member_id)} 유니크 제약으로 멱등성을 보장한다.
 *
 * <p>회원의 teamId/generationId 가 프로필 수정으로 가변이므로, 제출 시점의 소속을 스냅샷으로 보관하여 "본인 팀 제외"·집계 해석의 정합성을 유지한다.
 */
@Getter
@Entity
@Table(
        name = "vote_response",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_vote_response_vote_member",
                    columnNames = {"vote_id", "member_id"})
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @NotNull
    @Comment("투표 Id")
    @Column(name = "vote_id", nullable = false, columnDefinition = "bigint")
    private Long voteId;

    @NotNull
    @Comment("응답한 멤버 Id")
    @Column(name = "member_id", nullable = false, columnDefinition = "bigint")
    private Long memberId;

    @Comment("응답 시점에 고정된 템플릿 버전")
    @Column(name = "template_version", nullable = false, columnDefinition = "int")
    private int templateVersion;

    @Comment("제출 시점 소속 팀 Id 스냅샷")
    @Column(name = "snapshot_team_id", nullable = true, columnDefinition = "bigint")
    private Long snapshotTeamId;

    @Comment("제출 시점 소속 기수 Id 스냅샷")
    @Column(name = "snapshot_generation_id", nullable = true, columnDefinition = "bigint")
    private Long snapshotGenerationId;

    @Builder(access = AccessLevel.PRIVATE)
    public VoteResponse(
            Long voteId,
            Long memberId,
            int templateVersion,
            Long snapshotTeamId,
            Long snapshotGenerationId) {
        this.voteId = voteId;
        this.memberId = memberId;
        this.templateVersion = templateVersion;
        this.snapshotTeamId = snapshotTeamId;
        this.snapshotGenerationId = snapshotGenerationId;
    }

    public static VoteResponse create(
            final Long voteId,
            final Long memberId,
            final int templateVersion,
            final Long snapshotTeamId,
            final Long snapshotGenerationId) {
        return VoteResponse.builder()
                .voteId(voteId)
                .memberId(memberId)
                .templateVersion(templateVersion)
                .snapshotTeamId(snapshotTeamId)
                .snapshotGenerationId(snapshotGenerationId)
                .build();
    }
}
