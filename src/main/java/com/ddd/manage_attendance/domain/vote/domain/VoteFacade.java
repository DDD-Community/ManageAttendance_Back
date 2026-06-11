package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.util.TimeProvider;
import com.ddd.manage_attendance.domain.attendance.domain.Attendance;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceService;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceStatus;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import com.ddd.manage_attendance.domain.generation.domain.GenerationService;
import com.ddd.manage_attendance.domain.schedule.domain.ScheduleService;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import com.ddd.manage_attendance.domain.vote.api.dto.ActiveVoteResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.FeedbackResultResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.FeedbackTemplateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.MyVoteStatusResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.TeamVoteResultResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.TeamVoteTemplateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteCreateRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteDetailResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteNonRespondersResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteParticipationResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSummaryResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteTemplateUpdateRequest;
import com.ddd.manage_attendance.domain.vote.exception.VoteAlreadyOpenException;
import com.ddd.manage_attendance.domain.vote.exception.VoteManagerNotAllowedException;
import com.ddd.manage_attendance.domain.vote.exception.VoteNoActiveException;
import java.util.List;
import java.util.Map;
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
    private final GenerationService generationService;
    private final ScheduleService scheduleService;
    private final AttendanceService attendanceService;
    private final VoteAnswerValidator voteAnswerValidator;
    private final VoteTemplateValidator voteTemplateValidator;
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
        if (user.isManager()) {
            throw new VoteManagerNotAllowedException();
        }
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
        generationService.findById(request.generationId()); // 존재하지 않는 기수면 404
        voteTemplateValidator.validate(request.teamVoteTemplate(), request.feedbackTemplate());
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
        voteTemplateValidator.validate(request.teamVoteTemplate(), request.feedbackTemplate());
        final Vote vote = voteService.getVote(voteId);
        vote.updateTemplates(request.teamVoteTemplate(), request.feedbackTemplate());
    }

    @Transactional
    public void openVote(final Long userId, final Long voteId) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        final Vote vote = voteService.getVote(voteId);
        // 기수당 OPEN 투표는 하나라는 전제를 보장한다(/active 단일성).
        // 앱 레벨 가드: 운영진 수동·저빈도 작업이며, MySQL 은 조건부 유니크 인덱스를 지원하지 않아 DB 제약 대신 여기서 막는다.
        voteService
                .findOpenVote(vote.getGenerationId())
                .ifPresent(
                        open -> {
                            throw new VoteAlreadyOpenException();
                        });
        vote.open(timeProvider.nowDateTime());
    }

    @Transactional
    public void closeVote(final Long userId, final Long voteId) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        final Vote vote = voteService.getVote(voteId);
        vote.close(timeProvider.nowDateTime());
    }

    /** [멤버] 내 기수의 진행 중(OPEN) 투표 + 내 참여 여부. 진행 중 투표가 없으면 404. */
    @Transactional(readOnly = true)
    public ActiveVoteResponse getActiveVote(final Long userId) {
        final User user = userService.getUser(userId);
        final Vote vote =
                voteService
                        .findOpenVote(user.getGenerationId())
                        .orElseThrow(VoteNoActiveException::new);
        return ActiveVoteResponse.of(vote, voteService.hasResponded(vote.getId(), userId));
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

        final List<User> nonResponderUsers =
                members.stream().filter(m -> !respondedIds.contains(m.getId())).toList();
        final Map<Long, AttendanceStatus> todayStatusByMember =
                resolveTodayAttendance(vote.getGenerationId(), nonResponderUsers);

        final List<VoteNonRespondersResponse.NonResponder> nonResponders =
                nonResponderUsers.stream()
                        .map(
                                m ->
                                        new VoteNonRespondersResponse.NonResponder(
                                                m.getId(),
                                                m.getName(),
                                                teamNameById.get(m.getTeamId()),
                                                todayStatusByMember.getOrDefault(
                                                        m.getId(), AttendanceStatus.NONE)))
                        .toList();
        return VoteNonRespondersResponse.of(nonResponders);
    }

    /** 금일 일정이 있으면 대상 멤버들의 금일 출석 상태를 매핑한다. 일정이 없으면 빈 맵(→ 호출부에서 NONE 처리). */
    private Map<Long, AttendanceStatus> resolveTodayAttendance(
            final Long generationId, final List<User> users) {
        if (users.isEmpty()) {
            return Map.of();
        }
        return scheduleService
                .findScheduleByDateAndGenerationId(timeProvider.nowDate(), generationId)
                .map(
                        schedule -> {
                            final List<Long> userIds = users.stream().map(User::getId).toList();
                            return attendanceService
                                    .findAllUsersAttendancesByScheduleId(userIds, schedule.getId())
                                    .stream()
                                    .collect(
                                            Collectors.toMap(
                                                    Attendance::getUserId, Attendance::getStatus));
                        })
                .orElseGet(Map::of);
    }

    /** [운영진] 팀 투표 결과 집계(부문별 팀 득표 + 작성 사유). 집계 행을 템플릿 부문 메타·팀 정보와 머지한다. */
    @Transactional(readOnly = true)
    public TeamVoteResultResponse getTeamVoteResults(final Long userId, final Long voteId) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        final Vote vote = voteService.getVote(voteId);
        final List<Team> teams = teamService.findAllByGenerationId(vote.getGenerationId());
        return TeamVoteResultResponse.of(
                vote,
                teams,
                voteService.tallyTeamVotes(voteId),
                voteService.findTeamVoteReasons(voteId),
                voteService.countResponses(voteId));
    }

    /** [운영진] 참여 경험 피드백 결과 집계(질문 타입별 분포/텍스트). */
    @Transactional(readOnly = true)
    public FeedbackResultResponse getFeedbackResults(final Long userId, final Long voteId) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        final Vote vote = voteService.getVote(voteId);
        return FeedbackResultResponse.of(
                vote,
                voteService.tallyFeedbackOptions(voteId),
                voteService.tallyFeedbackBooleans(voteId),
                voteService.findFeedbackTexts(voteId),
                voteService.countResponses(voteId));
    }

    /** [운영진] 본인 기수의 투표 목록(최신순). */
    @Transactional(readOnly = true)
    public List<VoteSummaryResponse> getVotesForManager(final Long userId) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        return VoteSummaryResponse.fromList(
                voteService.findVotesByGeneration(manager.getGenerationId()));
    }

    /** [운영진] 투표 상세(상태 + 양쪽 템플릿). 편집 재개 화면에 사용한다. */
    @Transactional(readOnly = true)
    public VoteDetailResponse getVoteDetail(final Long userId, final Long voteId) {
        final User manager = userService.getUser(userId);
        manager.validateManager();
        return VoteDetailResponse.from(voteService.getVote(voteId));
    }
}
