package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserRepository;
import com.ddd.manage_attendance.domain.generation.domain.GenerationService;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import com.ddd.manage_attendance.domain.vote.api.dto.FeedbackResultResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.InternalVoteDetail;
import com.ddd.manage_attendance.domain.vote.api.dto.InternalVoteResponseExport;
import com.ddd.manage_attendance.domain.vote.api.dto.InternalVoteSummary;
import com.ddd.manage_attendance.domain.vote.api.dto.InternalVoteUpsertRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.TeamVoteResultResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteNonRespondersResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteParticipationResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalVoteFacade {
    private final VoteService voteService;
    private final VoteFacade voteFacade;
    private final GenerationService generationService;
    private final UserRepository userRepository;
    private final TeamService teamService;
    private final VoteResponseRepository voteResponseRepository;
    private final TeamVoteAnswerRepository teamVoteAnswerRepository;
    private final TeamVoteReasonRepository teamVoteReasonRepository;
    private final FeedbackAnswerRepository feedbackAnswerRepository;

    @Transactional(readOnly = true)
    public List<InternalVoteSummary> list(final Long generationId) {
        generationService.findById(generationId);
        // 관리자 투표 목록은 규모가 작아 응답 수는 투표별로 조회한다.
        return voteService.findVotesByGeneration(generationId).stream()
                .map(v -> InternalVoteSummary.of(v, voteService.countResponses(v.getId())))
                .toList();
    }

    @Transactional
    public InternalVoteDetail create(
            final Long generationId, final InternalVoteUpsertRequest request) {
        generationService.findById(generationId);
        final Vote vote =
                voteService.createDraft(
                        generationId,
                        request.title().trim(),
                        request.teamVoteTemplate(),
                        request.feedbackTemplate());
        return detail(vote);
    }

    @Transactional(readOnly = true)
    public InternalVoteDetail get(final Long voteId) {
        return detail(voteService.getVote(voteId));
    }

    @Transactional
    public InternalVoteDetail update(final Long voteId, final InternalVoteUpsertRequest request) {
        final Vote vote = voteService.getVote(voteId);
        voteService.updateDraft(
                vote,
                request.title().trim(),
                request.teamVoteTemplate(),
                request.feedbackTemplate());
        return detail(vote);
    }

    @Transactional
    public InternalVoteDetail reviseText(
            final Long voteId, final InternalVoteUpsertRequest request) {
        final Vote vote = voteService.getVote(voteId);
        voteService.reviseText(
                vote,
                request.title().trim(),
                request.teamVoteTemplate(),
                request.feedbackTemplate());
        return detail(vote);
    }

    @Transactional
    public void delete(final Long voteId) {
        voteService.delete(voteService.getVote(voteId));
    }

    @Transactional
    public InternalVoteDetail open(final Long voteId, final LocalDateTime now) {
        final Vote vote = voteService.getVote(voteId);
        voteService.open(vote, now);
        return detail(vote);
    }

    @Transactional
    public InternalVoteDetail close(final Long voteId, final LocalDateTime now) {
        final Vote vote = voteService.getVote(voteId);
        voteService.close(vote, now);
        return detail(vote);
    }

    @Transactional(readOnly = true)
    public VoteParticipationResponse participation(final Long voteId) {
        return voteFacade.participationOf(voteService.getVote(voteId));
    }

    @Transactional(readOnly = true)
    public VoteNonRespondersResponse nonResponders(final Long voteId) {
        return voteFacade.nonRespondersOf(voteService.getVote(voteId));
    }

    @Transactional(readOnly = true)
    public TeamVoteResultResponse teamResults(final Long voteId) {
        return voteFacade.teamVoteResultsOf(voteService.getVote(voteId));
    }

    @Transactional(readOnly = true)
    public FeedbackResultResponse feedbackResults(final Long voteId) {
        return voteFacade.feedbackResultsOf(voteService.getVote(voteId));
    }

    @Transactional(readOnly = true)
    public InternalVoteResponseExport responses(final Long voteId) {
        final Vote vote = voteService.getVote(voteId);
        final List<VoteResponse> responses =
                voteResponseRepository.findAllByVoteIdOrderByIdAsc(voteId);
        if (responses.isEmpty()) {
            return new InternalVoteResponseExport(
                    voteId,
                    vote.getTitle(),
                    vote.getTemplateVersion(),
                    vote.getTeamVoteTemplate(),
                    vote.getFeedbackTemplate(),
                    List.of());
        }
        final List<Long> responseIds = responses.stream().map(VoteResponse::getId).toList();
        final Map<Long, List<TeamVoteAnswer>> picks =
                teamVoteAnswerRepository.findAllByResponseIdInOrderByIdAsc(responseIds).stream()
                        .collect(Collectors.groupingBy(TeamVoteAnswer::getResponseId));
        final Map<Long, List<TeamVoteReason>> reasons =
                teamVoteReasonRepository.findAllByResponseIdInOrderByIdAsc(responseIds).stream()
                        .collect(Collectors.groupingBy(TeamVoteReason::getResponseId));
        final Map<Long, List<FeedbackAnswer>> feedback =
                feedbackAnswerRepository.findAllByResponseIdInOrderByIdAsc(responseIds).stream()
                        .collect(Collectors.groupingBy(FeedbackAnswer::getResponseId));
        final Map<Long, User> users =
                userRepository
                        .findAllById(responses.stream().map(VoteResponse::getMemberId).toList())
                        .stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
        final Map<Long, String> teamNames =
                teamService.findAllByGenerationId(vote.getGenerationId()).stream()
                        .collect(Collectors.toMap(Team::getId, Team::getName));
        final List<InternalVoteResponseExport.Response> rows =
                responses.stream()
                        .map(
                                r ->
                                        toExport(
                                                r,
                                                users.get(r.getMemberId()),
                                                teamNames,
                                                picks.getOrDefault(r.getId(), List.of()),
                                                reasons.getOrDefault(r.getId(), List.of()),
                                                feedback.getOrDefault(r.getId(), List.of())))
                        .toList();
        return new InternalVoteResponseExport(
                voteId,
                vote.getTitle(),
                vote.getTemplateVersion(),
                vote.getTeamVoteTemplate(),
                vote.getFeedbackTemplate(),
                rows);
    }

    private InternalVoteDetail detail(final Vote vote) {
        return InternalVoteDetail.of(vote, voteService.countResponses(vote.getId()));
    }

    private InternalVoteResponseExport.Response toExport(
            final VoteResponse response,
            final User user,
            final Map<Long, String> teamNames,
            final List<TeamVoteAnswer> picks,
            final List<TeamVoteReason> reasons,
            final List<FeedbackAnswer> answers) {
        final Map<String, List<TeamVoteAnswer>> picksByCategory =
                picks.stream()
                        .collect(
                                Collectors.groupingBy(
                                        TeamVoteAnswer::getCategoryId,
                                        LinkedHashMap::new,
                                        Collectors.toList()));
        final Map<String, TeamVoteReason> reasonByCategory =
                reasons.stream()
                        .collect(
                                Collectors.toMap(
                                        TeamVoteReason::getCategoryId,
                                        Function.identity(),
                                        (a, b) -> a));
        final List<InternalVoteResponseExport.TeamVote> teamVotes = new ArrayList<>();
        final LinkedHashSet<String> categoryIds = new LinkedHashSet<>();
        categoryIds.addAll(picksByCategory.keySet());
        categoryIds.addAll(reasonByCategory.keySet());
        for (final String categoryId : categoryIds) {
            final List<TeamVoteAnswer> categoryPicks =
                    picksByCategory.getOrDefault(categoryId, List.of());
            teamVotes.add(
                    new InternalVoteResponseExport.TeamVote(
                            categoryId,
                            categoryPicks.stream().map(TeamVoteAnswer::getTeamId).toList(),
                            categoryPicks.stream().map(a -> teamNames.get(a.getTeamId())).toList(),
                            reasonByCategory.containsKey(categoryId)
                                    ? reasonByCategory.get(categoryId).getReason()
                                    : null));
        }
        final Map<String, List<FeedbackAnswer>> answersByQuestion =
                answers.stream()
                        .collect(
                                Collectors.groupingBy(
                                        FeedbackAnswer::getQuestionId,
                                        LinkedHashMap::new,
                                        Collectors.toList()));
        final List<InternalVoteResponseExport.Feedback> feedbackRows =
                answersByQuestion.entrySet().stream()
                        .map(
                                e ->
                                        new InternalVoteResponseExport.Feedback(
                                                e.getKey(),
                                                e.getValue().stream()
                                                        .map(FeedbackAnswer::getOptionId)
                                                        .filter(Objects::nonNull)
                                                        .toList(),
                                                e.getValue().stream()
                                                        .map(FeedbackAnswer::getTextValue)
                                                        .filter(Objects::nonNull)
                                                        .findFirst()
                                                        .orElse(null),
                                                e.getValue().stream()
                                                        .map(FeedbackAnswer::getBoolValue)
                                                        .filter(Objects::nonNull)
                                                        .findFirst()
                                                        .orElse(null)))
                        .toList();
        return new InternalVoteResponseExport.Response(
                response.getId(),
                response.getMemberId(),
                user == null ? null : user.getName(),
                response.getSnapshotTeamId(),
                teamNames.get(response.getSnapshotTeamId()),
                response.getTemplateVersion(),
                response.getCreatedDate(),
                teamVotes,
                feedbackRows);
    }
}
