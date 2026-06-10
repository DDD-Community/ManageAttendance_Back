package com.ddd.manage_attendance.domain.vote.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.ddd.manage_attendance.domain.vote.exception.VoteAlreadyRespondedException;
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
}
