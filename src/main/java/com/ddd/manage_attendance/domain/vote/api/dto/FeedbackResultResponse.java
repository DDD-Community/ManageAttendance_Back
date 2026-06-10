package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.VoteComponentType;
import com.ddd.manage_attendance.domain.vote.domain.FeedbackBoolTally;
import com.ddd.manage_attendance.domain.vote.domain.FeedbackOptionTally;
import com.ddd.manage_attendance.domain.vote.domain.FeedbackTextView;
import com.ddd.manage_attendance.domain.vote.domain.Vote;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * [운영진] 참여 경험 피드백 결과 집계 응답. 질문 타입에 따라 집계 형태가 다르다.
 *
 * <ul>
 *   <li>MULTI_SELECT — 템플릿의 모든 선택지를 0 포함 분포로 노출
 *   <li>BOOLEAN — 예/아니오 응답 수
 *   <li>LONG_TEXT — 작성된 텍스트 목록(익명)
 * </ul>
 *
 * <p>followUp(중첩 후속 질문)도 응답 대상이므로 평탄화하여 함께 집계한다.
 */
@Schema(title = "[투표] 참여 경험 피드백 결과 집계 응답 DTO")
public record FeedbackResultResponse(
        @Schema(description = "투표 Id", example = "1") Long voteId,
        @Schema(description = "총 응답자 수", example = "35") long totalResponses,
        @Schema(description = "질문별 결과(followUp 포함, 템플릿 order 순)") List<QuestionResult> questions) {

    @Schema(title = "질문별 결과")
    public record QuestionResult(
            @Schema(description = "질문 Id") String questionId,
            @Schema(description = "질문 제목") String title,
            @Schema(description = "컴포넌트 타입") VoteComponentType type,
            @Schema(description = "노출 순서") int order,
            @Schema(description = "선택지별 응답 수(MULTI_SELECT, 그 외 null)") List<OptionResult> options,
            @Schema(description = "예 응답 수(BOOLEAN, 그 외 null)") Long trueCount,
            @Schema(description = "아니오 응답 수(BOOLEAN, 그 외 null)") Long falseCount,
            @Schema(description = "작성 응답 목록(LONG_TEXT, 그 외 null)") List<String> textAnswers) {}

    @Schema(title = "선택지별 응답 수")
    public record OptionResult(
            @Schema(description = "선택지 Id") String optionId,
            @Schema(description = "선택지 라벨") String label,
            @Schema(description = "응답 수") long count) {}

    public static FeedbackResultResponse of(
            final Vote vote,
            final List<FeedbackOptionTally> optionTallies,
            final List<FeedbackBoolTally> boolTallies,
            final List<FeedbackTextView> texts,
            final long totalResponses) {
        final Map<String, Map<String, Long>> optionCount =
                optionTallies.stream()
                        .collect(
                                Collectors.groupingBy(
                                        FeedbackOptionTally::questionId,
                                        Collectors.toMap(
                                                FeedbackOptionTally::optionId,
                                                FeedbackOptionTally::count)));
        final Map<String, Map<Boolean, Long>> boolCount =
                boolTallies.stream()
                        .collect(
                                Collectors.groupingBy(
                                        FeedbackBoolTally::questionId,
                                        Collectors.toMap(
                                                FeedbackBoolTally::boolValue,
                                                FeedbackBoolTally::count)));
        final Map<String, List<String>> textsByQuestion =
                texts.stream()
                        .collect(
                                Collectors.groupingBy(
                                        FeedbackTextView::questionId,
                                        Collectors.mapping(
                                                FeedbackTextView::textValue, Collectors.toList())));

        final FeedbackTemplate template = vote.getFeedbackTemplate();
        final List<QuestionResult> questions =
                template == null
                        ? List.of()
                        : flatten(template.questions()).stream()
                                .map(
                                        question ->
                                                toResult(
                                                        question,
                                                        optionCount,
                                                        boolCount,
                                                        textsByQuestion))
                                .filter(Objects::nonNull)
                                .toList();
        return new FeedbackResultResponse(vote.getId(), totalResponses, questions);
    }

    private static QuestionResult toResult(
            final FeedbackTemplate.Question question,
            final Map<String, Map<String, Long>> optionCount,
            final Map<String, Map<Boolean, Long>> boolCount,
            final Map<String, List<String>> textsByQuestion) {
        return switch (question.type()) {
            case MULTI_SELECT ->
                    new QuestionResult(
                            question.id(),
                            question.title(),
                            question.type(),
                            question.order(),
                            options(question, optionCount.getOrDefault(question.id(), Map.of())),
                            null,
                            null,
                            null);
            case BOOLEAN -> {
                final Map<Boolean, Long> counts = boolCount.getOrDefault(question.id(), Map.of());
                yield new QuestionResult(
                        question.id(),
                        question.title(),
                        question.type(),
                        question.order(),
                        null,
                        counts.getOrDefault(Boolean.TRUE, 0L),
                        counts.getOrDefault(Boolean.FALSE, 0L),
                        null);
            }
            case LONG_TEXT ->
                    new QuestionResult(
                            question.id(),
                            question.title(),
                            question.type(),
                            question.order(),
                            null,
                            null,
                            null,
                            textsByQuestion.getOrDefault(question.id(), List.of()));
            case TEAM_SELECT -> null;
        };
    }

    private static List<OptionResult> options(
            final FeedbackTemplate.Question question, final Map<String, Long> counts) {
        if (question.options() == null) {
            return List.of();
        }
        return question.options().stream()
                .map(
                        option ->
                                new OptionResult(
                                        option.id(),
                                        option.label(),
                                        counts.getOrDefault(option.id(), 0L)))
                .toList();
    }

    private static List<FeedbackTemplate.Question> flatten(
            final List<FeedbackTemplate.Question> questions) {
        final List<FeedbackTemplate.Question> flat = new ArrayList<>();
        questions.stream()
                .sorted(Comparator.comparingInt(FeedbackTemplate.Question::order))
                .forEach(question -> appendWithFollowUps(question, flat));
        return flat;
    }

    private static void appendWithFollowUps(
            final FeedbackTemplate.Question question, final List<FeedbackTemplate.Question> flat) {
        flat.add(question);
        if (question.followUp() != null) {
            appendWithFollowUps(question.followUp(), flat);
        }
    }
}
