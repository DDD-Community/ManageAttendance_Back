package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.util.TimeProvider;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import com.ddd.manage_attendance.domain.vote.api.dto.ActiveVoteResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.FeedbackTemplateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.MyVoteStatusResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.TeamVoteTemplateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteCreateRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteNonRespondersResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteParticipationResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteTemplateUpdateRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    /** [멤버] 내 기수의 진행 중(OPEN) 투표 + 내 참여 여부. 홈 메뉴에 '투표' 노출/진입 판단에 사용한다. */
    @Transactional(readOnly = true)
    public ActiveVoteResponse getActiveVote(final Long userId) {
        final User user = userService.getUser(userId);
        final Optional<Vote> activeVote = voteService.findOpenVote(user.getGenerationId());
        return activeVote
                .map(
                        vote ->
                                ActiveVoteResponse.of(
                                        vote, voteService.hasResponded(vote.getId(), userId)))
                .orElseGet(ActiveVoteResponse::none);
    }

    /** [멤버] 특정 투표에 내가 이미 참여했는지 조회. 완료/재참여 차단 화면 판단에 사용한다. */
    @Transactional(readOnly = true)
    public MyVoteStatusResponse getMyVoteStatus(final Long userId, final Long voteId) {
        userService.getUser(userId);
        voteService.getVote(voteId);
        return MyVoteStatusResponse.of(voteId, voteService.hasResponded(voteId, userId));
    }

    /** [운영진] 투표 상태 + 참여 현황(대상/참여/참여율). 운영진은 운영진 본인을 제외한 멤버를 모집단으로 본다. */
    @Transactional(readOnly = true)
    public VoteParticipationResponse getParticipation(final Long userId, final Long voteId) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        final Vote vote = voteService.getVote(voteId);

        final List<User> members = userService.findMembersByGeneration(vote.getGenerationId());
        final Set<Long> respondedIds = voteService.findRespondedMemberIds(voteId);
        final int respondedMembers =
                (int) members.stream().filter(m -> respondedIds.contains(m.getId())).count();
        return VoteParticipationResponse.of(vote, members.size(), respondedMembers);
    }

    /** [운영진] 미참여 멤버 명단(이름 + 소속 팀명). */
    @Transactional(readOnly = true)
    public VoteNonRespondersResponse getNonResponders(final Long userId, final Long voteId) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        final Vote vote = voteService.getVote(voteId);

        final List<User> members = userService.findMembersByGeneration(vote.getGenerationId());
        final Set<Long> respondedIds = voteService.findRespondedMemberIds(voteId);
        final Map<Long, String> teamNameById =
                teamService.findAllByGenerationId(vote.getGenerationId()).stream()
                        .collect(Collectors.toMap(Team::getId, Team::getName));

        final List<VoteNonRespondersResponse.NonResponder> nonResponders =
                members.stream()
                        .filter(m -> !respondedIds.contains(m.getId()))
                        .map(
                                m ->
                                        new VoteNonRespondersResponse.NonResponder(
                                                m.getId(),
                                                m.getName(),
                                                teamNameById.get(m.getTeamId())))
                        .toList();
        return VoteNonRespondersResponse.of(nonResponders);
    }
}
