package com.ddd.manage_attendance.domain.vote.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ddd.manage_attendance.domain.vote.exception.VoteAlreadyOpenException;
import com.ddd.manage_attendance.domain.vote.exception.VoteAlreadyRespondedException;
import com.ddd.manage_attendance.domain.vote.exception.VoteNotDraftException;
import com.ddd.manage_attendance.domain.vote.exception.VoteTemplateInvalidException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock private VoteRepository voteRepository;
    @Mock private VoteResponseRepository voteResponseRepository;
    @Mock private TeamVoteAnswerRepository teamVoteAnswerRepository;
    @Mock private TeamVoteReasonRepository teamVoteReasonRepository;
    @Mock private FeedbackAnswerRepository feedbackAnswerRepository;
    @Mock private VoteTemplateValidator voteTemplateValidator;
    @InjectMocks private VoteService voteService;

    @Test
    @DisplayName("이미 응답한 멤버가 다시 제출하면 예외가 발생한다")
    void submitResponse_whenAlreadyResponded_throws() {
        final Vote vote = mock(Vote.class);
        given(vote.getId()).willReturn(1L);
        given(voteResponseRepository.existsByVoteIdAndMemberId(1L, 10L)).willReturn(true);

        assertThatThrownBy(() -> voteService.submitResponse(vote, 10L, 100L, 13L, null))
                .isInstanceOf(VoteAlreadyRespondedException.class);
    }

    @Test
    @DisplayName("동시 제출로 유니크 제약을 위반하면 이미 참여한 것으로 처리한다")
    void submitResponse_whenUniqueViolation_throwsAlreadyResponded() {
        final Vote vote = mock(Vote.class);
        given(vote.getId()).willReturn(1L);
        given(vote.getTemplateVersion()).willReturn(1);
        given(voteResponseRepository.existsByVoteIdAndMemberId(1L, 10L)).willReturn(false);
        given(voteResponseRepository.save(any()))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> voteService.submitResponse(vote, 10L, 100L, 13L, null))
                .isInstanceOf(VoteAlreadyRespondedException.class);
    }

    @Test
    @DisplayName("기수에 OPEN 투표가 있으면 open 은 이미 열린 예외를 던진다")
    void open_whenVoteExists_throws() {
        final Vote draft = Vote.createDraft(13L, "투표", null, null);
        given(voteRepository.findFirstByGenerationIdAndStatusOrderByIdDesc(13L, VoteStatus.OPEN))
                .willReturn(Optional.of(mock(Vote.class)));

        assertThatThrownBy(() -> voteService.open(draft, LocalDateTime.now()))
                .isInstanceOf(VoteAlreadyOpenException.class);
    }

    @Test
    @DisplayName("기수에 OPEN 투표가 없으면 open 한다")
    void open_whenNoVote_opens() {
        final Vote draft = Vote.createDraft(13L, "투표", null, null);
        final LocalDateTime now = LocalDateTime.of(2026, 1, 2, 3, 4);
        given(voteRepository.findFirstByGenerationIdAndStatusOrderByIdDesc(13L, VoteStatus.OPEN))
                .willReturn(Optional.empty());

        voteService.open(draft, now);

        assertThat(draft.getStatus()).isEqualTo(VoteStatus.OPEN);
        assertThat(draft.getOpenedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("createDraft 는 템플릿 검증 예외를 전파한다")
    void createDraft_whenInvalidTemplate_propagates() {
        willThrow(new VoteTemplateInvalidException("잘못된 템플릿"))
                .given(voteTemplateValidator)
                .validate(null, null);

        assertThatThrownBy(() -> voteService.createDraft(13L, "투표", null, null))
                .isInstanceOf(VoteTemplateInvalidException.class);
        verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateDraft 는 템플릿 검증 예외를 전파한다")
    void updateDraft_whenInvalidTemplate_propagates() {
        final Vote draft = Vote.createDraft(13L, "투표", null, null);
        willThrow(new VoteTemplateInvalidException("잘못된 템플릿"))
                .given(voteTemplateValidator)
                .validate(null, null);

        assertThatThrownBy(() -> voteService.updateDraft(draft, "새 제목", null, null))
                .isInstanceOf(VoteTemplateInvalidException.class);
        assertThat(draft.getTitle()).isEqualTo("투표");
    }

    @Test
    @DisplayName("reviseText 는 템플릿 검증 예외를 전파한다")
    void reviseText_whenInvalidTemplate_propagates() {
        final Vote open = Vote.createDraft(13L, "투표", null, null);
        open.open(LocalDateTime.now());
        willThrow(new VoteTemplateInvalidException("잘못된 템플릿"))
                .given(voteTemplateValidator)
                .validate(null, null);

        assertThatThrownBy(() -> voteService.reviseText(open, "새 제목", null, null))
                .isInstanceOf(VoteTemplateInvalidException.class);
        assertThat(open.getTitle()).isEqualTo("투표");
    }

    @Test
    @DisplayName("DRAFT 가 아닌 투표 삭제는 거부한다")
    void delete_whenNotDraft_rejects() {
        final Vote open = Vote.createDraft(13L, "투표", null, null);
        open.open(LocalDateTime.now());

        assertThatThrownBy(() -> voteService.delete(open))
                .isInstanceOf(VoteNotDraftException.class);
        verify(voteRepository, never()).delete(any(Vote.class));
    }

    @Test
    @DisplayName("DRAFT 투표 삭제는 저장소에 위임한다")
    void delete_whenDraft_deletes() {
        final Vote draft = Vote.createDraft(13L, "투표", null, null);

        voteService.delete(draft);

        verify(voteRepository).delete(draft);
    }
}
