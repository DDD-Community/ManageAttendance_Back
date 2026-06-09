package com.ddd.manage_attendance.domain.vote.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddd.manage_attendance.domain.vote.exception.VoteInvalidStatusException;
import com.ddd.manage_attendance.domain.vote.exception.VoteNotDraftException;
import com.ddd.manage_attendance.domain.vote.exception.VoteNotOpenException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VoteTest {

    private Vote draftVote() {
        return Vote.createDraft(1L, "DDD 13기 투표", null, null);
    }

    @Test
    @DisplayName("생성 시 DRAFT 상태와 버전 1로 시작한다")
    void createDraft_initsDraftAndVersionOne() {
        final Vote vote = draftVote();

        assertThat(vote.getStatus()).isEqualTo(VoteStatus.DRAFT);
        assertThat(vote.getTemplateVersion()).isEqualTo(1);
        assertThat(vote.isDraft()).isTrue();
    }

    @Test
    @DisplayName("DRAFT 에서 open 하면 OPEN 으로 전환되고 시작 시각이 기록된다")
    void open_fromDraft_transitionsToOpen() {
        final Vote vote = draftVote();
        final LocalDateTime now = LocalDateTime.of(2026, 6, 9, 10, 0);

        vote.open(now);

        assertThat(vote.getStatus()).isEqualTo(VoteStatus.OPEN);
        assertThat(vote.getOpenedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("DRAFT 가 아닐 때 open 하면 예외가 발생한다")
    void open_whenNotDraft_throws() {
        final Vote vote = draftVote();
        vote.open(LocalDateTime.now());

        assertThatThrownBy(() -> vote.open(LocalDateTime.now()))
                .isInstanceOf(VoteInvalidStatusException.class);
    }

    @Test
    @DisplayName("OPEN 에서 close 하면 CLOSED 로 전환된다")
    void close_fromOpen_transitionsToClosed() {
        final Vote vote = draftVote();
        vote.open(LocalDateTime.now());
        final LocalDateTime now = LocalDateTime.of(2026, 6, 9, 12, 0);

        vote.close(now);

        assertThat(vote.getStatus()).isEqualTo(VoteStatus.CLOSED);
        assertThat(vote.getClosedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("OPEN 이 아닐 때 close 하면 예외가 발생한다")
    void close_whenNotOpen_throws() {
        final Vote vote = draftVote();

        assertThatThrownBy(() -> vote.close(LocalDateTime.now()))
                .isInstanceOf(VoteInvalidStatusException.class);
    }

    @Test
    @DisplayName("DRAFT 에서 템플릿 수정 시 버전이 증가한다")
    void updateTemplates_inDraft_bumpsVersion() {
        final Vote vote = draftVote();

        vote.updateTemplates(null, null);

        assertThat(vote.getTemplateVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("DRAFT 가 아닐 때 템플릿 수정 시 예외가 발생한다")
    void updateTemplates_whenNotDraft_throws() {
        final Vote vote = draftVote();
        vote.open(LocalDateTime.now());

        assertThatThrownBy(() -> vote.updateTemplates(null, null))
                .isInstanceOf(VoteNotDraftException.class);
    }

    @Test
    @DisplayName("OPEN 이 아닐 때 validateOpen 은 예외를 던진다")
    void validateOpen_whenNotOpen_throws() {
        final Vote vote = draftVote();

        assertThatThrownBy(vote::validateOpen).isInstanceOf(VoteNotOpenException.class);
    }
}
