package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest.FeedbackQuestionAnswer;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest.TeamVoteCategoryAnswer;
import com.ddd.manage_attendance.domain.vote.exception.OwnTeamSelectedException;
import com.ddd.manage_attendance.domain.vote.exception.VoteAnswerInvalidException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 제출된 응답을 투표가 보유한 불변 템플릿으로 서버에서 재검증한다. (클라이언트 제약은 UX 용일 뿐, 신뢰 경계는 서버)
 *
 * <p>사유(reason)는 "해당 부문에 팀을 선택한 경우"에만 필수로 본다. 필수 피드백 질문은 최상위 질문에 한해 강제한다.
 */
@Component
public class VoteAnswerValidator {

    public void validate(
            final Vote vote,
            final Long ownTeamId,
            final Set<Long> validTeamIds,
            final VoteSubmitRequest request) {
        validateTeamVote(
                vote.getTeamVoteTemplate(), ownTeamId, validTeamIds, request.teamVoteOrEmpty());
        validateFeedback(vote.getFeedbackTemplate(), request.feedbackOrEmpty());
    }

    private void validateTeamVote(
            final TeamVoteTemplate template,
            final Long ownTeamId,
            final Set<Long> validTeamIds,
            final List<TeamVoteCategoryAnswer> answers) {
        if (template == null) {
            if (!answers.isEmpty()) {
                throw new VoteAnswerInvalidException("팀 투표 템플릿이 존재하지 않습니다.");
            }
            return;
        }

        final Map<String, TeamVoteTemplate.Category> categories =
                template.categories().stream()
                        .collect(
                                Collectors.toMap(
                                        TeamVoteTemplate.Category::id, Function.identity()));

        final Set<String> seenCategoryIds = new HashSet<>();
        for (final TeamVoteCategoryAnswer answer : answers) {
            final TeamVoteTemplate.Category category = categories.get(answer.categoryId());
            if (category == null) {
                throw new VoteAnswerInvalidException("존재하지 않는 부문입니다: " + answer.categoryId());
            }
            if (!seenCategoryIds.add(answer.categoryId())) {
                throw new VoteAnswerInvalidException("부문이 중복 제출되었습니다: " + answer.categoryId());
            }

            final List<Long> teamIds = answer.teamIds() == null ? List.of() : answer.teamIds();
            if (teamIds.size() != new HashSet<>(teamIds).size()) {
                throw new VoteAnswerInvalidException("같은 팀을 중복 선택했습니다.");
            }
            if (teamIds.size() > category.maxSelectableTeams()) {
                throw new VoteAnswerInvalidException(
                        "부문 '%s' 은 최대 %d개 팀까지 선택할 수 있습니다."
                                .formatted(category.title(), category.maxSelectableTeams()));
            }
            for (final Long teamId : teamIds) {
                if (Objects.equals(teamId, ownTeamId)) {
                    throw new OwnTeamSelectedException();
                }
                if (!validTeamIds.contains(teamId)) {
                    throw new VoteAnswerInvalidException("선택할 수 없는 팀입니다: " + teamId);
                }
            }
            validateReason(category, answer.reason(), !teamIds.isEmpty());
        }
    }

    private void validateReason(
            final TeamVoteTemplate.Category category,
            final String reason,
            final boolean teamSelected) {
        final boolean hasReason = StringUtils.hasText(reason);
        if (category.reasonRequired() && teamSelected && !hasReason) {
            throw new VoteAnswerInvalidException(
                    "부문 '%s' 의 사유를 입력해주세요.".formatted(category.title()));
        }
        if (hasReason) {
            final int length = reason.trim().length();
            if (length < category.reasonMinLength()) {
                throw new VoteAnswerInvalidException(
                        "부문 '%s' 의 사유는 최소 %d자 이상 입력해주세요."
                                .formatted(category.title(), category.reasonMinLength()));
            }
            if (length > category.reasonMaxLength()) {
                throw new VoteAnswerInvalidException(
                        "부문 '%s' 의 사유는 최대 %d자까지 입력할 수 있습니다."
                                .formatted(category.title(), category.reasonMaxLength()));
            }
        }
    }

    private void validateFeedback(
            final FeedbackTemplate template, final List<FeedbackQuestionAnswer> answers) {
        if (template == null) {
            if (!answers.isEmpty()) {
                throw new VoteAnswerInvalidException("피드백 템플릿이 존재하지 않습니다.");
            }
            return;
        }

        final Map<String, FeedbackTemplate.Question> questionById =
                indexQuestions(template.questions());
        final Map<String, FeedbackQuestionAnswer> answerById = new LinkedHashMap<>();
        for (final FeedbackQuestionAnswer answer : answers) {
            final FeedbackTemplate.Question question = questionById.get(answer.questionId());
            if (question == null) {
                throw new VoteAnswerInvalidException("존재하지 않는 질문입니다: " + answer.questionId());
            }
            if (answerById.put(answer.questionId(), answer) != null) {
                throw new VoteAnswerInvalidException("질문이 중복 제출되었습니다: " + answer.questionId());
            }
            validateAnswerByType(question, answer);
        }

        for (final FeedbackTemplate.Question question : template.questions()) {
            if (question.required() && !hasValue(question, answerById.get(question.id()))) {
                throw new VoteAnswerInvalidException("필수 질문에 응답해주세요: " + question.title());
            }
        }
    }

    private Map<String, FeedbackTemplate.Question> indexQuestions(
            final List<FeedbackTemplate.Question> questions) {
        final Map<String, FeedbackTemplate.Question> map = new LinkedHashMap<>();
        for (final FeedbackTemplate.Question question : questions) {
            indexQuestion(question, map);
        }
        return map;
    }

    // follow-up 은 재귀적으로 중첩될 수 있으므로 끝까지 인덱싱한다.
    private void indexQuestion(
            final FeedbackTemplate.Question question,
            final Map<String, FeedbackTemplate.Question> map) {
        map.put(question.id(), question);
        if (question.followUp() != null) {
            indexQuestion(question.followUp(), map);
        }
    }

    private void validateAnswerByType(
            final FeedbackTemplate.Question question, final FeedbackQuestionAnswer answer) {
        final boolean hasOptions = answer.optionIds() != null && !answer.optionIds().isEmpty();
        final boolean hasText = StringUtils.hasText(answer.textValue());
        final boolean hasBool = answer.boolValue() != null;

        switch (question.type()) {
            case MULTI_SELECT -> {
                if (hasText || hasBool) {
                    throw mismatchedFields(question);
                }
                validateMultiSelect(question, answer);
            }
            case LONG_TEXT -> {
                if (hasOptions || hasBool) {
                    throw mismatchedFields(question);
                }
                validateLongText(question, answer);
            }
            case BOOLEAN -> {
                // boolValue 존재 여부만 의미가 있으며, 선택지/텍스트가 함께 오면 안 된다.
                if (hasOptions || hasText) {
                    throw mismatchedFields(question);
                }
            }
            case TEAM_SELECT ->
                    throw new VoteAnswerInvalidException(
                            "피드백 질문에 허용되지 않는 타입입니다: " + question.title());
        }
    }

    private VoteAnswerInvalidException mismatchedFields(final FeedbackTemplate.Question question) {
        return new VoteAnswerInvalidException(
                "질문 '%s' 에 타입과 맞지 않는 값이 함께 전달되었습니다.".formatted(question.title()));
    }

    private void validateMultiSelect(
            final FeedbackTemplate.Question question, final FeedbackQuestionAnswer answer) {
        final List<String> optionIds = answer.optionIds() == null ? List.of() : answer.optionIds();
        if (optionIds.size() != new HashSet<>(optionIds).size()) {
            throw new VoteAnswerInvalidException(
                    "질문 '%s' 에서 같은 선택지를 중복 선택했습니다.".formatted(question.title()));
        }
        final Set<String> validOptionIds =
                question.options() == null
                        ? Set.of()
                        : question.options().stream()
                                .map(FeedbackTemplate.Option::id)
                                .collect(Collectors.toSet());
        for (final String optionId : optionIds) {
            if (!validOptionIds.contains(optionId)) {
                throw new VoteAnswerInvalidException(
                        "질문 '%s' 에 없는 선택지입니다: %s".formatted(question.title(), optionId));
            }
        }
        if (question.maxSelectableOptions() != null
                && optionIds.size() > question.maxSelectableOptions()) {
            throw new VoteAnswerInvalidException(
                    "질문 '%s' 은 최대 %d개까지 선택할 수 있습니다."
                            .formatted(question.title(), question.maxSelectableOptions()));
        }
    }

    private void validateLongText(
            final FeedbackTemplate.Question question, final FeedbackQuestionAnswer answer) {
        if (StringUtils.hasText(answer.textValue())
                && question.maxLength() != null
                && answer.textValue().length() > question.maxLength()) {
            throw new VoteAnswerInvalidException(
                    "질문 '%s' 은 최대 %d자까지 입력할 수 있습니다."
                            .formatted(question.title(), question.maxLength()));
        }
    }

    private boolean hasValue(
            final FeedbackTemplate.Question question, final FeedbackQuestionAnswer answer) {
        if (answer == null) {
            return false;
        }
        return switch (question.type()) {
            case MULTI_SELECT -> answer.optionIds() != null && !answer.optionIds().isEmpty();
            case LONG_TEXT -> StringUtils.hasText(answer.textValue());
            case BOOLEAN -> answer.boolValue() != null;
            case TEAM_SELECT -> false;
        };
    }
}
