package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.vote.domain.TeamReasonView;
import com.ddd.manage_attendance.domain.vote.domain.TeamVoteTally;
import com.ddd.manage_attendance.domain.vote.domain.Vote;
import com.ddd.manage_attendance.domain.vote.domain.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * [운영진] 팀 투표 결과 집계 응답. 정규화된 집계 행({@link TeamVoteTally}/{@link TeamReasonView})을 투표가 보유한 불변 템플릿의 부문
 * 메타(제목/순서)와 머지한다. 부문은 템플릿 순서대로, 팀은 득표수 내림차순으로 노출한다.
 */
@Schema(title = "[투표] 팀 투표 결과 집계 응답 DTO")
public record TeamVoteResultResponse(
        @Schema(description = "투표 Id", example = "1") Long voteId,
        @Schema(description = "투표 제목", example = "DDD 13기 최종 투표") String title,
        @Schema(description = "투표 상태 (DRAFT/OPEN/CLOSED)") VoteStatus status,
        @Schema(description = "총 응답자 수", example = "35") long totalResponses,
        @Schema(description = "부문별 결과(템플릿 order 순)") List<CategoryResult> categories) {

    @Schema(title = "부문별 결과")
    public record CategoryResult(
            @Schema(description = "부문 Id") String categoryId,
            @Schema(description = "부문명", example = "기획 완성도") String title,
            @Schema(description = "노출 순서") int order,
            @Schema(description = "팀별 득표(득표수 내림차순)") List<TeamResult> teams,
            @Schema(description = "작성된 사유 목록(익명)") List<String> reasons) {}

    @Schema(title = "팀별 득표")
    public record TeamResult(
            @Schema(description = "순위(1부터, 득표수 내림차순)") int rank,
            @Schema(description = "팀 Id") Long teamId,
            @Schema(description = "팀명", example = "iOS 1팀") String name,
            @Schema(description = "서비스명", example = "PICKFLOW") String serviceName,
            @Schema(description = "득표수") long voteCount) {}

    public static TeamVoteResultResponse of(
            final Vote vote,
            final List<Team> teams,
            final List<TeamVoteTally> tallies,
            final List<TeamReasonView> reasons,
            final long totalResponses) {
        final Map<Long, Team> teamById =
                teams.stream().collect(Collectors.toMap(Team::getId, Function.identity()));
        final Map<String, List<TeamVoteTally>> talliesByCategory =
                tallies.stream().collect(Collectors.groupingBy(TeamVoteTally::categoryId));
        final Map<String, List<String>> reasonsByCategory =
                reasons.stream()
                        .collect(
                                Collectors.groupingBy(
                                        TeamReasonView::categoryId,
                                        Collectors.mapping(
                                                TeamReasonView::reason, Collectors.toList())));

        final TeamVoteTemplate template = vote.getTeamVoteTemplate();
        final List<CategoryResult> categories =
                template == null
                        ? List.of()
                        : template.categories().stream()
                                .sorted(Comparator.comparingInt(TeamVoteTemplate.Category::order))
                                .map(
                                        category ->
                                                new CategoryResult(
                                                        category.id(),
                                                        category.title(),
                                                        category.order(),
                                                        rank(
                                                                talliesByCategory.getOrDefault(
                                                                        category.id(), List.of()),
                                                                teamById),
                                                        reasonsByCategory.getOrDefault(
                                                                category.id(), List.of())))
                                .toList();
        return new TeamVoteResultResponse(
                vote.getId(), vote.getTitle(), vote.getStatus(), totalResponses, categories);
    }

    private static List<TeamResult> rank(
            final List<TeamVoteTally> categoryTallies, final Map<Long, Team> teamById) {
        final List<TeamVoteTally> sorted =
                categoryTallies.stream()
                        .sorted(Comparator.comparingLong(TeamVoteTally::voteCount).reversed())
                        .toList();
        final List<TeamResult> results = new ArrayList<>();
        int rank = 1;
        for (final TeamVoteTally tally : sorted) {
            final Team team = teamById.get(tally.teamId());
            results.add(
                    new TeamResult(
                            rank++,
                            tally.teamId(),
                            team == null ? null : team.getName(),
                            team == null ? null : team.getServiceName(),
                            tally.voteCount()));
        }
        return results;
    }
}
