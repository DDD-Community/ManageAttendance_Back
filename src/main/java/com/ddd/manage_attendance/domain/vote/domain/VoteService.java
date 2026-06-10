package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest.FeedbackQuestionAnswer;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest.TeamVoteCategoryAnswer;
import com.ddd.manage_attendance.domain.vote.exception.VoteAlreadyRespondedException;
import com.ddd.manage_attendance.domain.vote.exception.VoteNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteResponseRepository voteResponseRepository;
    private final TeamVoteAnswerRepository teamVoteAnswerRepository;
    private final TeamVoteReasonRepository teamVoteReasonRepository;
    private final FeedbackAnswerRepository feedbackAnswerRepository;

    @Transactional(readOnly = true)
    public Vote getVote(final Long voteId) {
        return voteRepository.findById(voteId).orElseThrow(VoteNotFoundException::new);
    }

    /** 기수의 진행 중(OPEN) 투표. 멤버 메뉴 노출/진입 판단에 사용한다. */
    @Transactional(readOnly = true)
    public Optional<Vote> findOpenVote(final Long generationId) {
        return voteRepository.findFirstByGenerationIdAndStatusOrderByIdDesc(
                generationId, VoteStatus.OPEN);
    }

    /** 멤버가 해당 투표에 이미 응답했는지 여부. */
    @Transactional(readOnly = true)
    public boolean hasResponded(final Long voteId, final Long memberId) {
        return voteResponseRepository.existsByVoteIdAndMemberId(voteId, memberId);
    }

    /** 해당 투표에 응답한 멤버 Id 집합. 참여 현황/미참여 명단 집계에 사용한다. */
    @Transactional(readOnly = true)
    public Set<Long> findRespondedMemberIds(final Long voteId) {
        return voteResponseRepository.findAllByVoteId(voteId).stream()
                .map(VoteResponse::getMemberId)
                .collect(Collectors.toSet());
    }

    /** 한 기수의 전체 투표(최신순). 운영진 투표 목록에 사용한다. */
    @Transactional(readOnly = true)
    public List<Vote> findVotesByGeneration(final Long generationId) {
        return voteRepository.findAllByGenerationIdOrderByIdDesc(generationId);
    }

    /** 해당 투표의 총 응답자 수. 결과 집계의 분모로 사용한다. */
    @Transactional(readOnly = true)
    public long countResponses(final Long voteId) {
        return voteResponseRepository.countByVoteId(voteId);
    }

    /** 부문별 팀 득표 집계. */
    @Transactional(readOnly = true)
    public List<TeamVoteTally> tallyTeamVotes(final Long voteId) {
        return teamVoteAnswerRepository.tallyByVoteId(voteId);
    }

    /** 부문별 작성 사유 목록(익명). */
    @Transactional(readOnly = true)
    public List<TeamReasonView> findTeamVoteReasons(final Long voteId) {
        return teamVoteReasonRepository.findReasonsByVoteId(voteId);
    }

    /** 피드백 MULTI_SELECT 선택지별 응답 수 집계. */
    @Transactional(readOnly = true)
    public List<FeedbackOptionTally> tallyFeedbackOptions(final Long voteId) {
        return feedbackAnswerRepository.tallyOptionsByVoteId(voteId);
    }

    /** 피드백 BOOLEAN 예/아니오 응답 수 집계. */
    @Transactional(readOnly = true)
    public List<FeedbackBoolTally> tallyFeedbackBooleans(final Long voteId) {
        return feedbackAnswerRepository.tallyBoolByVoteId(voteId);
    }

    /** 피드백 LONG_TEXT 작성 응답 목록(익명). */
    @Transactional(readOnly = true)
    public List<FeedbackTextView> findFeedbackTexts(final Long voteId) {
        return feedbackAnswerRepository.findTextsByVoteId(voteId);
    }

    @Transactional
    public Vote createDraft(
            final Long generationId,
            final String title,
            final TeamVoteTemplate teamVoteTemplate,
            final FeedbackTemplate feedbackTemplate) {
        return voteRepository.save(
                Vote.createDraft(generationId, title, teamVoteTemplate, feedbackTemplate));
    }

    /** 멱등 제출. (vote_id, member_id) 유니크 제약 + 동시성 예외 처리로 1인 1응답을 보장한다. 재투표는 거부한다. */
    @Transactional
    public void submitResponse(
            final Vote vote,
            final Long memberId,
            final Long snapshotTeamId,
            final Long snapshotGenerationId,
            final VoteSubmitRequest request) {
        if (voteResponseRepository.existsByVoteIdAndMemberId(vote.getId(), memberId)) {
            throw new VoteAlreadyRespondedException();
        }

        final VoteResponse response;
        try {
            response =
                    voteResponseRepository.save(
                            VoteResponse.create(
                                    vote.getId(),
                                    memberId,
                                    vote.getTemplateVersion(),
                                    snapshotTeamId,
                                    snapshotGenerationId));
        } catch (DataIntegrityViolationException e) {
            // 동시 제출로 유니크 제약 위반 -> 이미 참여한 것으로 처리
            throw new VoteAlreadyRespondedException();
        }

        saveTeamVoteAnswers(response.getId(), request.teamVoteOrEmpty());
        saveFeedbackAnswers(response.getId(), request.feedbackOrEmpty());
    }

    private void saveTeamVoteAnswers(
            final Long responseId, final List<TeamVoteCategoryAnswer> answers) {
        final List<TeamVoteAnswer> picks = new ArrayList<>();
        final List<TeamVoteReason> reasons = new ArrayList<>();
        for (final TeamVoteCategoryAnswer answer : answers) {
            if (answer.teamIds() != null) {
                for (final Long teamId : answer.teamIds()) {
                    picks.add(TeamVoteAnswer.create(responseId, answer.categoryId(), teamId));
                }
            }
            if (StringUtils.hasText(answer.reason())) {
                reasons.add(
                        TeamVoteReason.create(
                                responseId, answer.categoryId(), answer.reason().trim()));
            }
        }
        teamVoteAnswerRepository.saveAll(picks);
        teamVoteReasonRepository.saveAll(reasons);
    }

    private void saveFeedbackAnswers(
            final Long responseId, final List<FeedbackQuestionAnswer> answers) {
        final List<FeedbackAnswer> rows = new ArrayList<>();
        for (final FeedbackQuestionAnswer answer : answers) {
            if (answer.optionIds() != null) {
                for (final String optionId : answer.optionIds()) {
                    rows.add(FeedbackAnswer.ofOption(responseId, answer.questionId(), optionId));
                }
            }
            if (StringUtils.hasText(answer.textValue())) {
                rows.add(
                        FeedbackAnswer.ofText(
                                responseId, answer.questionId(), answer.textValue().trim()));
            }
            if (answer.boolValue() != null) {
                rows.add(
                        FeedbackAnswer.ofBool(responseId, answer.questionId(), answer.boolValue()));
            }
        }
        feedbackAnswerRepository.saveAll(rows);
    }
}
