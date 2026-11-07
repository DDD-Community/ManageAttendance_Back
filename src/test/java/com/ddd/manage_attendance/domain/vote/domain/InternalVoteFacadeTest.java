package com.ddd.manage_attendance.domain.vote.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserRepository;
import com.ddd.manage_attendance.domain.generation.domain.GenerationService;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import com.ddd.manage_attendance.domain.vote.api.dto.InternalVoteUpsertRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InternalVoteFacadeTest {
    @Mock private VoteService voteService;
    @Mock private VoteFacade voteFacade;
    @Mock private GenerationService generationService;
    @Mock private UserRepository userRepository;
    @Mock private TeamService teamService;
    @Mock private VoteResponseRepository voteResponseRepository;
    @Mock private TeamVoteAnswerRepository teamVoteAnswerRepository;
    @Mock private TeamVoteReasonRepository teamVoteReasonRepository;
    @Mock private FeedbackAnswerRepository feedbackAnswerRepository;
    @InjectMocks private InternalVoteFacade facade;

    @Test
    void list_whenGenerationIsMissing_returnsNotFound() {
        given(generationService.findById(999L)).willThrow(new DataNotFoundException());

        assertThatThrownBy(() -> facade.list(999L)).isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void responses_whenNoRows_returnsEmptyExport() {
        final Vote vote = Vote.createDraft(1L, "투표", null, null);
        given(voteService.getVote(1L)).willReturn(vote);
        given(voteResponseRepository.findAllByVoteIdOrderByIdAsc(1L)).willReturn(List.of());

        final var result = facade.responses(1L);

        assertThat(result.responses()).isEmpty();
        assertThat(result.voteId()).isEqualTo(1L);
    }

    @Test
    void responses_preserveResponseAndAnswerOrderAndGroupRows() {
        final TeamVoteTemplate teamTemplate = new TeamVoteTemplate("팀", null, null, List.of());
        final FeedbackTemplate feedbackTemplate = new FeedbackTemplate("피드백", null, List.of());
        final Vote vote = Vote.createDraft(13L, "투표", teamTemplate, feedbackTemplate);
        ReflectionTestUtils.setField(vote, "id", 1L);
        final VoteResponse first = response(101L, 11L, 500L, 1);
        final VoteResponse second = response(102L, 12L, 999L, 2);
        given(voteService.getVote(1L)).willReturn(vote);
        given(voteResponseRepository.findAllByVoteIdOrderByIdAsc(1L))
                .willReturn(List.of(first, second));
        final TeamVoteAnswer categoryB = teamAnswer(101L, "B", 202L, 2L);
        final TeamVoteAnswer categoryA = teamAnswer(101L, "A", 201L, 3L);
        given(teamVoteAnswerRepository.findAllByResponseIdInOrderByIdAsc(List.of(101L, 102L)))
                .willReturn(List.of(categoryB, categoryA));
        final TeamVoteReason reason = TeamVoteReason.create(101L, "B", "선택 이유");
        ReflectionTestUtils.setField(reason, "id", 4L);
        given(teamVoteReasonRepository.findAllByResponseIdInOrderByIdAsc(List.of(101L, 102L)))
                .willReturn(List.of(reason));
        final FeedbackAnswer option1 = FeedbackAnswer.ofOption(101L, "multi", "o1");
        final FeedbackAnswer option2 = FeedbackAnswer.ofOption(101L, "multi", "o2");
        final FeedbackAnswer text = FeedbackAnswer.ofText(101L, "text", "장문 답변");
        final FeedbackAnswer bool = FeedbackAnswer.ofBool(101L, "bool", true);
        ReflectionTestUtils.setField(option1, "id", 5L);
        ReflectionTestUtils.setField(option2, "id", 6L);
        ReflectionTestUtils.setField(text, "id", 7L);
        ReflectionTestUtils.setField(bool, "id", 8L);
        given(feedbackAnswerRepository.findAllByResponseIdInOrderByIdAsc(List.of(101L, 102L)))
                .willReturn(List.of(option1, option2, text, bool));
        final User user = mock(User.class);
        given(user.getId()).willReturn(11L);
        given(user.getName()).willReturn("홍길동");
        given(userRepository.findAllById(List.of(11L, 12L))).willReturn(List.of(user));
        final Team team = Team.createTeam("팀 이름", 13L);
        ReflectionTestUtils.setField(team, "id", 202L);
        given(teamService.findAllByGenerationId(13L)).willReturn(List.of(team));

        final var result = facade.responses(1L);

        assertThat(result.responses()).extracting(r -> r.responseId()).containsExactly(101L, 102L);
        assertThat(result.responses().get(0).memberName()).isEqualTo("홍길동");
        assertThat(result.responses().get(1).memberName()).isNull();
        assertThat(result.responses().get(1).teamName()).isNull();
        assertThat(result.responses().get(0).teamVote())
                .extracting(r -> r.categoryId())
                .containsExactly("B", "A");
        assertThat(result.responses().get(0).teamVote().get(0).teamIds()).containsExactly(202L);
        assertThat(result.responses().get(0).teamVote().get(0).reason()).isEqualTo("선택 이유");
        assertThat(result.responses().get(0).teamVote().get(1).reason()).isNull();
        assertThat(result.responses().get(0).feedback())
                .extracting(r -> r.questionId())
                .containsExactly("multi", "text", "bool");
        assertThat(result.responses().get(0).feedback().get(0).optionIds())
                .containsExactly("o1", "o2");
        assertThat(result.responses().get(0).feedback().get(1).textValue()).isEqualTo("장문 답변");
        assertThat(result.responses().get(0).feedback().get(2).boolValue()).isTrue();
    }

    @Test
    void create_whenGenerationMissing_propagatesException() {
        given(generationService.findById(999L)).willThrow(new DataNotFoundException());
        final InternalVoteUpsertRequest request = new InternalVoteUpsertRequest("제목", null, null);

        assertThatThrownBy(() -> facade.create(999L, request))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void deleteOpenAndClose_delegateToVoteService() {
        final Vote vote = Vote.createDraft(13L, "투표", null, null);
        ReflectionTestUtils.setField(vote, "id", 1L);
        given(voteService.getVote(1L)).willReturn(vote);
        given(voteService.countResponses(1L)).willReturn(0L);
        final LocalDateTime now = LocalDateTime.of(2026, 1, 2, 3, 4);

        facade.delete(1L);
        facade.open(1L, now);
        facade.close(1L, now);

        verify(voteService).delete(vote);
        verify(voteService).open(vote, now);
        verify(voteService).close(vote, now);
    }

    private static VoteResponse response(
            final Long id,
            final Long memberId,
            final Long snapshotTeamId,
            final int templateVersion) {
        final VoteResponse response =
                VoteResponse.create(1L, memberId, templateVersion, snapshotTeamId, 13L);
        ReflectionTestUtils.setField(response, "id", id);
        ReflectionTestUtils.setField(response, "createdDate", LocalDateTime.of(2026, 1, 2, 3, 4));
        return response;
    }

    private static TeamVoteAnswer teamAnswer(
            final Long responseId, final String categoryId, final Long teamId, final Long id) {
        final TeamVoteAnswer answer = TeamVoteAnswer.create(responseId, categoryId, teamId);
        ReflectionTestUtils.setField(answer, "id", id);
        return answer;
    }
}
