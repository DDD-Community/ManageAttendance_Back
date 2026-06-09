package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.util.TimeProvider;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import com.ddd.manage_attendance.domain.vote.api.dto.FeedbackTemplateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.TeamVoteTemplateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteCreateRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteTemplateUpdateRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteFacade {

    private final VoteService voteService;
    private final UserService userService;
    private final TeamService teamService;
    private final VoteAnswerValidator voteAnswerValidator;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public TeamVoteTemplateResponse getTeamVoteTemplate(final Long userId, final Long voteId) {
        final User user = userService.getUser(userId);
        final Vote vote = voteService.getVote(voteId);
        final List<Team> teams = teamService.findAllByGenerationId(vote.getGenerationId());
        return TeamVoteTemplateResponse.from(vote, teams, user.getTeamId());
    }

    @Transactional(readOnly = true)
    public FeedbackTemplateResponse getFeedbackTemplate(final Long userId, final Long voteId) {
        userService.getUser(userId);
        final Vote vote = voteService.getVote(voteId);
        return FeedbackTemplateResponse.from(vote);
    }

    @Transactional
    public void submit(final Long userId, final Long voteId, final VoteSubmitRequest request) {
        final User user = userService.getUser(userId);
        final Vote vote = voteService.getVote(voteId);
        vote.validateOpen();

        final List<Team> teams = teamService.findAllByGenerationId(vote.getGenerationId());
        final Set<Long> validTeamIds = teams.stream().map(Team::getId).collect(Collectors.toSet());

        voteAnswerValidator.validate(vote, user.getTeamId(), validTeamIds, request);
        voteService.submitResponse(
                vote, user.getId(), user.getTeamId(), user.getGenerationId(), request);
    }

    @Transactional
    public Long createVote(final Long userId, final VoteCreateRequest request) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        final Vote vote =
                voteService.createDraft(
                        request.generationId(),
                        request.title(),
                        request.teamVoteTemplate(),
                        request.feedbackTemplate());
        return vote.getId();
    }

    @Transactional
    public void updateTemplate(
            final Long userId, final Long voteId, final VoteTemplateUpdateRequest request) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        final Vote vote = voteService.getVote(voteId);
        vote.updateTemplates(request.teamVoteTemplate(), request.feedbackTemplate());
    }

    @Transactional
    public void openVote(final Long userId, final Long voteId) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        final Vote vote = voteService.getVote(voteId);
        vote.open(timeProvider.nowDateTime());
    }

    @Transactional
    public void closeVote(final Long userId, final Long voteId) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        final Vote vote = voteService.getVote(voteId);
        vote.close(timeProvider.nowDateTime());
    }
}
