package com.ddd.manage_attendance.domain.vote.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.vote.domain.TeamReasonView;
import com.ddd.manage_attendance.domain.vote.domain.TeamVoteTally;
import com.ddd.manage_attendance.domain.vote.domain.Vote;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TeamVoteResultResponseTest {

    private static final Long GENERATION_ID = 13L;

    private Team team(final Long id, final String name, final String serviceName) {
        final Team team = Team.createTeam(name, GENERATION_ID, serviceName);
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    private TeamVoteTemplate singleCategoryTemplate() {
        return new TeamVoteTemplate(
                "팀 투표",
                null,
                null,
                List.of(new TeamVoteTemplate.Category("c1", 1, "기획 완성도", 2, false, 0, 500, "이유")));
    }

    @Test
    @DisplayName("부문별 팀 득표를 득표수 내림차순으로 순위 매겨 반환하고 사유를 매핑한다")
    void of_ranksTeamsByVoteCountDescAndMapsReasons() {
        final Vote vote = Vote.createDraft(GENERATION_ID, "최종 투표", singleCategoryTemplate(), null);
        final Team teamA = team(100L, "A팀", "서비스A");
        final Team teamB = team(101L, "B팀", "서비스B");
        final List<TeamVoteTally> tallies =
                List.of(new TeamVoteTally("c1", 100L, 3L), new TeamVoteTally("c1", 101L, 5L));
        final List<TeamReasonView> reasons = List.of(new TeamReasonView("c1", "잘했어요"));

        final TeamVoteResultResponse response =
                TeamVoteResultResponse.of(vote, List.of(teamA, teamB), tallies, reasons, 8L);

        assertThat(response.totalResponses()).isEqualTo(8L);
        assertThat(response.categories()).hasSize(1);

        final TeamVoteResultResponse.CategoryResult category = response.categories().get(0);
        assertThat(category.categoryId()).isEqualTo("c1");
        assertThat(category.teams())
                .extracting(TeamVoteResultResponse.TeamResult::teamId)
                .containsExactly(101L, 100L);
        assertThat(category.teams().get(0).rank()).isEqualTo(1);
        assertThat(category.teams().get(0).name()).isEqualTo("B팀");
        assertThat(category.teams().get(0).serviceName()).isEqualTo("서비스B");
        assertThat(category.teams().get(0).voteCount()).isEqualTo(5L);
        assertThat(category.teams().get(1).rank()).isEqualTo(2);
        assertThat(category.reasons()).containsExactly("잘했어요");
    }

    @Test
    @DisplayName("득표가 없는 팀은 결과에서 제외한다")
    void of_excludesTeamsWithoutVotes() {
        final Vote vote = Vote.createDraft(GENERATION_ID, "최종 투표", singleCategoryTemplate(), null);
        final Team teamA = team(100L, "A팀", null);
        final Team teamB = team(101L, "B팀", null);
        final List<TeamVoteTally> tallies = List.of(new TeamVoteTally("c1", 100L, 2L));

        final TeamVoteResultResponse response =
                TeamVoteResultResponse.of(vote, List.of(teamA, teamB), tallies, List.of(), 2L);

        assertThat(response.categories().get(0).teams())
                .extracting(TeamVoteResultResponse.TeamResult::teamId)
                .containsExactly(100L);
    }

    @Test
    @DisplayName("팀 투표 템플릿이 없으면 빈 부문 목록을 반환한다")
    void of_whenNoTemplate_returnsEmptyCategories() {
        final Vote vote = Vote.createDraft(GENERATION_ID, "투표", null, null);

        final TeamVoteResultResponse response =
                TeamVoteResultResponse.of(vote, List.of(), List.of(), List.of(), 0L);

        assertThat(response.categories()).isEmpty();
    }
}
