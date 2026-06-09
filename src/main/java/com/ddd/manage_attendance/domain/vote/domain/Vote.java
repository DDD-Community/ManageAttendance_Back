package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.domain.vote.exception.VoteInvalidStatusException;
import com.ddd.manage_attendance.domain.vote.exception.VoteNotDraftException;
import com.ddd.manage_attendance.domain.vote.exception.VoteNotOpenException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/**
 * 투표(한 기수에 하나씩 열림). 라이프사이클(상태·기수)을 소유하며, 관심사가 다른 두 영역(팀 투표 / 참여 경험 피드백)의 템플릿을 불변 스냅샷으로 보유한다.
 *
 * <p>템플릿은 DRAFT 상태에서만 수정 가능하며, OPEN 시점에 고정(freeze)된다. 응답은 자신이 답한 {@code templateVersion} 을 핀하여 과거
 * 해석을 보장한다.
 */
@Getter
@Entity
@Table(name = "vote")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @NotNull
    @Comment("기수 Id")
    @Column(name = "generation_id", nullable = false, columnDefinition = "bigint")
    private Long generationId;

    @NotNull
    @Comment("투표 제목")
    @Column(name = "title", nullable = false, columnDefinition = "varchar(100)")
    private String title;

    @NotNull
    @Comment("투표 상태 (DRAFT/OPEN/CLOSED)")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20)")
    private VoteStatus status;

    @Comment("템플릿 버전 (수정 시마다 증가)")
    @Column(name = "template_version", nullable = false, columnDefinition = "int")
    private int templateVersion;

    @Comment("팀 투표 템플릿 (JSON)")
    @Convert(converter = TeamVoteTemplateConverter.class)
    @Column(name = "team_vote_template", columnDefinition = "text")
    private TeamVoteTemplate teamVoteTemplate;

    @Comment("참여 경험 피드백 템플릿 (JSON)")
    @Convert(converter = FeedbackTemplateConverter.class)
    @Column(name = "feedback_template", columnDefinition = "text")
    private FeedbackTemplate feedbackTemplate;

    @Comment("투표 시작 시각")
    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Comment("투표 종료 시각")
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Builder(access = AccessLevel.PRIVATE)
    public Vote(
            Long generationId,
            String title,
            VoteStatus status,
            int templateVersion,
            TeamVoteTemplate teamVoteTemplate,
            FeedbackTemplate feedbackTemplate) {
        this.generationId = generationId;
        this.title = title;
        this.status = status;
        this.templateVersion = templateVersion;
        this.teamVoteTemplate = teamVoteTemplate;
        this.feedbackTemplate = feedbackTemplate;
    }

    public static Vote createDraft(
            final Long generationId,
            final String title,
            final TeamVoteTemplate teamVoteTemplate,
            final FeedbackTemplate feedbackTemplate) {
        return Vote.builder()
                .generationId(generationId)
                .title(title)
                .status(VoteStatus.DRAFT)
                .templateVersion(1)
                .teamVoteTemplate(teamVoteTemplate)
                .feedbackTemplate(feedbackTemplate)
                .build();
    }

    /** 템플릿 수정. DRAFT 상태에서만 허용되며 버전을 증가시킨다. */
    public void updateTemplates(
            final TeamVoteTemplate teamVoteTemplate, final FeedbackTemplate feedbackTemplate) {
        validateDraft();
        this.teamVoteTemplate = teamVoteTemplate;
        this.feedbackTemplate = feedbackTemplate;
        this.templateVersion += 1;
    }

    /** 투표 시작. DRAFT → OPEN (템플릿 freeze). */
    public void open(final LocalDateTime now) {
        if (this.status != VoteStatus.DRAFT) {
            throw new VoteInvalidStatusException();
        }
        this.status = VoteStatus.OPEN;
        this.openedAt = now;
    }

    /** 투표 종료. OPEN → CLOSED (불가역). */
    public void close(final LocalDateTime now) {
        if (this.status != VoteStatus.OPEN) {
            throw new VoteInvalidStatusException();
        }
        this.status = VoteStatus.CLOSED;
        this.closedAt = now;
    }

    public boolean isDraft() {
        return this.status == VoteStatus.DRAFT;
    }

    public boolean isOpen() {
        return this.status == VoteStatus.OPEN;
    }

    public void validateDraft() {
        if (!isDraft()) {
            throw new VoteNotDraftException();
        }
    }

    public void validateOpen() {
        if (!isOpen()) {
            throw new VoteNotOpenException();
        }
    }
}
