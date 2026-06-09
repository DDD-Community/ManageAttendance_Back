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
