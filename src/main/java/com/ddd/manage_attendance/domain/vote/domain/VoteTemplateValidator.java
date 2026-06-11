package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.core.sdui.VoteComponentType;
import com.ddd.manage_attendance.domain.vote.exception.VoteTemplateInvalidException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 투표 생성/수정 시 템플릿을 "작성 시점"에 검증한다. (제출 시점의 {@link VoteAnswerValidator} 가 템플릿이 올바르다고 신뢰하기 때문)
 *
 * <p>여기서 막지 않으면 잘못된 템플릿이 그대로 저장·freeze 되어, 제출 단계에서야 500 또는 "응답 불가능한 투표"로 드러난다.
 */
@Component
public class VoteTemplateValidator {

    public void validate(
            final TeamVoteTemplate teamVoteTemplate, final FeedbackTemplate feedbackTemplate) {
        if (teamVoteTemplate == null && feedbackTemplate == null) {
            throw new VoteTemplateInvalidException("팀 투표 또는 피드백 템플릿 중 하나 이상이 필요합니다.");
        }
        if (teamVoteTemplate != null) {
            validateTeamVoteTemplate(teamVoteTemplate);
        }
        if (feedbackTemplate != null) {
            validateFeedbackTemplate(feedbackTemplate);
        }
    }

    private void validateTeamVoteTemplate(final TeamVoteTemplate template) {
        final List<TeamVoteTemplate.Category> categories = template.categories();
        if (categories == null || categories.isEmpty()) {
            throw new VoteTemplateInvalidException("팀 투표 부문이 하나 이상 필요합니다.");
        }

        final Set<String> seenCategoryIds = new HashSet<>();
        for (final TeamVoteTemplate.Category category : categories) {
            if (!StringUtils.hasText(category.id())) {
                throw new VoteTemplateInvalidException("부문 ID는 비어 있을 수 없습니다.");
            }
            if (!seenCategoryIds.add(category.id())) {
                throw new VoteTemplateInvalidException("부문 ID가 중복되었습니다: " + category.id());
            }
            if (category.maxSelectableTeams() < 1) {
                throw new VoteTemplateInvalidException(
                        "부문 '%s' 의 최대 선택 팀 수는 1 이상이어야 합니다.".formatted(category.title()));
            }
            if (category.reasonMinLength() < 0
                    || category.reasonMinLength() > category.reasonMaxLength()) {
                throw new VoteTemplateInvalidException(
                        "부문 '%s' 의 사유 글자수 범위가 올바르지 않습니다.".formatted(category.title()));
            }
        }
    }

    private void validateFeedbackTemplate(final FeedbackTemplate template) {
        final List<FeedbackTemplate.Question> questions = template.questions();
        if (questions == null || questions.isEmpty()) {
            throw new VoteTemplateInvalidException("피드백 질문이 하나 이상 필요합니다.");
        }

        final Set<String> seenQuestionIds = new HashSet<>();
        for (final FeedbackTemplate.Question question : questions) {
            validateQuestion(question, seenQuestionIds);
        }
    }

    /** follow-up 까지 재귀로 검증한다. JSON 은 트리라 순환이 없으므로 재귀는 항상 종료된다. */
    private void validateQuestion(
            final FeedbackTemplate.Question question, final Set<String> seenQuestionIds) {
        if (!StringUtils.hasText(question.id())) {
            throw new VoteTemplateInvalidException("질문 ID는 비어 있을 수 없습니다.");
        }
        if (!seenQuestionIds.add(question.id())) {
            throw new VoteTemplateInvalidException("질문 ID가 중복되었습니다: " + question.id());
        }
        if (question.type() == null) {
            throw new VoteTemplateInvalidException(
                    "질문 '%s' 의 타입이 비어 있습니다.".formatted(question.title()));
        }
        if (question.type() == VoteComponentType.TEAM_SELECT) {
            throw new VoteTemplateInvalidException("피드백 질문에 허용되지 않는 타입입니다: " + question.title());
        }
        if (question.type() == VoteComponentType.MULTI_SELECT) {
            validateOptions(question);
        }
        if (question.maxLength() != null && question.maxLength() < 1) {
            throw new VoteTemplateInvalidException(
                    "질문 '%s' 의 최대 글자수는 1 이상이어야 합니다.".formatted(question.title()));
        }
        if (question.followUp() != null) {
            validateQuestion(question.followUp(), seenQuestionIds);
        }
    }

    private void validateOptions(final FeedbackTemplate.Question question) {
        final List<FeedbackTemplate.Option> options = question.options();
        if (options == null || options.isEmpty()) {
            throw new VoteTemplateInvalidException(
                    "다중 선택 질문 '%s' 은 선택지가 하나 이상 필요합니다.".formatted(question.title()));
        }
        final Set<String> seenOptionIds = new HashSet<>();
        for (final FeedbackTemplate.Option option : options) {
            if (!StringUtils.hasText(option.id())) {
                throw new VoteTemplateInvalidException(
                        "질문 '%s' 의 선택지 ID는 비어 있을 수 없습니다.".formatted(question.title()));
            }
            if (!seenOptionIds.add(option.id())) {
                throw new VoteTemplateInvalidException(
                        "질문 '%s' 의 선택지 ID가 중복되었습니다: %s".formatted(question.title(), option.id()));
            }
        }
        if (question.maxSelectableOptions() != null && question.maxSelectableOptions() < 1) {
            throw new VoteTemplateInvalidException(
                    "질문 '%s' 의 최대 선택 개수는 1 이상이어야 합니다.".formatted(question.title()));
        }
    }
}
