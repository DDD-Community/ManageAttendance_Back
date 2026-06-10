package com.ddd.manage_attendance.domain.vote.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ddd.manage_attendance.core.util.TimeProvider;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import com.ddd.manage_attendance.domain.vote.api.dto.ActiveVoteResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteNonRespondersResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteParticipationResponse;
import com.ddd.manage_attendance.domain.vote.exception.VoteAlreadyOpenException;
import com.ddd.manage_attendance.domain.vote.exception.VoteManagerNotAllowedException;
import com.ddd.manage_attendance.domain.vote.exception.VoteNoActiveException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VoteFacadeTest {

    @Mock private VoteService voteService;
    @Mock private UserService userService;
    @Mock private TeamService teamService;
    @Mock private VoteAnswerValidator voteAnswerValidator;
    @Mock private TimeProvider timeProvider;
    @InjectMocks private VoteFacade voteFacade;

    private static final Long USER_ID = 1L;
    private static final Long VOTE_ID = 100L;
    private static final Long GENERATION_ID = 13L;

    @Test
    @DisplayName("운영진이 투표를 제출하면 예외가 발생하고 응답은 저장되지 않는다")
    void submit_whenManager_throws() {
        final User manager = mock(User.class);
        given(userService.getUser(USER_ID)).willReturn(manager);
        given(manager.isManager()).willReturn(true);

        assertThatThrownBy(() -> voteFacade.submit(USER_ID, VOTE_ID, null))
                .isInstanceOf(VoteManagerNotAllowedException.class);
        verify(voteService, never()).submitResponse(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("진행 중 투표가 없으면 getActiveVote 는 예외를 던진다")
    void getActiveVote_whenNoOpen_throws() {
        final User user = mock(User.class);
        given(userService.getUser(USER_ID)).willReturn(user);
        given(user.getGenerationId()).willReturn(GENERATION_ID);
        given(voteService.findOpenVote(GENERATION_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> voteFacade.getActiveVote(USER_ID))
                .isInstanceOf(VoteNoActiveException.class);
    }

    @Test
    @DisplayName("진행 중 투표가 있으면 참여 여부와 함께 반환한다")
    void getActiveVote_whenOpen_returns() {
        final User user = mock(User.class);
        final Vote vote = Vote.createDraft(GENERATION_ID, "최종 투표", null, null);
        ReflectionTestUtils.setField(vote, "id", VOTE_ID);
        given(userService.getUser(USER_ID)).willReturn(user);
        given(user.getGenerationId()).willReturn(GENERATION_ID);
        given(voteService.findOpenVote(GENERATION_ID)).willReturn(Optional.of(vote));
        given(voteService.hasResponded(VOTE_ID, USER_ID)).willReturn(true);

        final ActiveVoteResponse response = voteFacade.getActiveVote(USER_ID);

        assertThat(response.voteId()).isEqualTo(VOTE_ID);
        assertThat(response.title()).isEqualTo("최종 투표");
        assertThat(response.alreadyResponded()).isTrue();
    }

    @Test
    @DisplayName("같은 기수에 진행 중 투표가 있으면 open 시 예외가 발생하고 상태는 DRAFT 로 유지된다")
    void openVote_whenAlreadyOpen_throws() {
        final User manager = mock(User.class);
        final Vote draft = Vote.createDraft(GENERATION_ID, "새 투표", null, null);
        given(userService.getUser(USER_ID)).willReturn(manager);
        given(voteService.getVote(VOTE_ID)).willReturn(draft);
        given(voteService.findOpenVote(GENERATION_ID)).willReturn(Optional.of(mock(Vote.class)));

        assertThatThrownBy(() -> voteFacade.openVote(USER_ID, VOTE_ID))
                .isInstanceOf(VoteAlreadyOpenException.class);
        assertThat(draft.getStatus()).isEqualTo(VoteStatus.DRAFT);
    }

    @Test
    @DisplayName("진행 중 투표가 없으면 open 에 성공해 OPEN 으로 전환된다")
    void openVote_whenNoOpen_success() {
        final User manager = mock(User.class);
        final Vote draft = Vote.createDraft(GENERATION_ID, "새 투표", null, null);
        final LocalDateTime now = LocalDateTime.of(2026, 6, 10, 10, 0);
        given(userService.getUser(USER_ID)).willReturn(manager);
        given(voteService.getVote(VOTE_ID)).willReturn(draft);
        given(voteService.findOpenVote(GENERATION_ID)).willReturn(Optional.empty());
        given(timeProvider.nowDateTime()).willReturn(now);

        voteFacade.openVote(USER_ID, VOTE_ID);

        assertThat(draft.getStatus()).isEqualTo(VoteStatus.OPEN);
        assertThat(draft.getOpenedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("참여 현황은 운영진 제외 멤버를 모집단으로 참여율을 계산한다")
    void getParticipation_computesRateOverMembers() {
        final User manager = mock(User.class);
        final Vote vote = Vote.createDraft(GENERATION_ID, "투표", null, null);
        final User m1 = mock(User.class);
        final User m2 = mock(User.class);
        final User m3 = mock(User.class);
        given(m1.getId()).willReturn(10L);
        given(m2.getId()).willReturn(11L);
        given(m3.getId()).willReturn(12L);
        given(userService.getUser(USER_ID)).willReturn(manager);
        given(voteService.getVote(VOTE_ID)).willReturn(vote);
        given(userService.findMembersByGeneration(GENERATION_ID)).willReturn(List.of(m1, m2, m3));
        given(voteService.findRespondedMemberIds(VOTE_ID)).willReturn(Set.of(10L));

        final VoteParticipationResponse response = voteFacade.getParticipation(USER_ID, VOTE_ID);

        assertThat(response.totalMembers()).isEqualTo(3);
        assertThat(response.respondedMembers()).isEqualTo(1);
        assertThat(response.participationRate()).isEqualTo(33);
    }

    @Test
    @DisplayName("미참여 명단은 응답하지 않은 멤버만 소속 팀명과 함께 반환한다")
    void getNonResponders_filtersAndMapsTeamName() {
        final User manager = mock(User.class);
        final Vote vote = Vote.createDraft(GENERATION_ID, "투표", null, null);
        final User responded = mock(User.class);
        final User pendingWithTeam = mock(User.class);
        final User pendingNoTeam = mock(User.class);
        given(responded.getId()).willReturn(10L);
        given(pendingWithTeam.getId()).willReturn(11L);
        given(pendingWithTeam.getName()).willReturn("김길동");
        given(pendingWithTeam.getTeamId()).willReturn(101L);
        given(pendingNoTeam.getId()).willReturn(12L);
        given(pendingNoTeam.getName()).willReturn("이몽룡");
        given(pendingNoTeam.getTeamId()).willReturn(null);
        final Team teamB = Team.createTeam("B팀", GENERATION_ID);
        ReflectionTestUtils.setField(teamB, "id", 101L);

        given(userService.getUser(USER_ID)).willReturn(manager);
        given(voteService.getVote(VOTE_ID)).willReturn(vote);
        given(userService.findMembersByGeneration(GENERATION_ID))
                .willReturn(List.of(responded, pendingWithTeam, pendingNoTeam));
        given(voteService.findRespondedMemberIds(VOTE_ID)).willReturn(Set.of(10L));
        given(teamService.findAllByGenerationId(GENERATION_ID)).willReturn(List.of(teamB));

        final VoteNonRespondersResponse response = voteFacade.getNonResponders(USER_ID, VOTE_ID);

        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.members())
                .extracting(VoteNonRespondersResponse.NonResponder::name)
                .containsExactlyInAnyOrder("김길동", "이몽룡");
        assertThat(response.members())
                .filteredOn(r -> r.memberId().equals(11L))
                .singleElement()
                .extracting(VoteNonRespondersResponse.NonResponder::teamName)
                .isEqualTo("B팀");
        assertThat(response.members())
                .filteredOn(r -> r.memberId().equals(12L))
                .singleElement()
                .extracting(VoteNonRespondersResponse.NonResponder::teamName)
                .isNull();
    }
}
