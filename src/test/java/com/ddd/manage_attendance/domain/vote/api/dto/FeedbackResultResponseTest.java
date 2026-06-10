package com.ddd.manage_attendance.domain.vote.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.VoteComponentType;
import com.ddd.manage_attendance.domain.vote.domain.FeedbackBoolTally;
import com.ddd.manage_attendance.domain.vote.domain.FeedbackOptionTally;
import com.ddd.manage_attendance.domain.vote.domain.FeedbackTextView;
import com.ddd.manage_attendance.domain.vote.domain.Vote;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FeedbackResultResponseTest {

    private static final Long GENERATION_ID = 13L;

    private Vote voteWith(final FeedbackTemplate template) {
        return Vote.createDraft(GENERATION_ID, "투표", null, template);
    }

    @Test
    @DisplayName("MULTI_SELECT 은 템플릿의 모든 선택지를 0 포함 분포로 집계한다")
    void of_multiSelect_includesAllOptionsWithZero() {
        final FeedbackTemplate.Question question =
                new FeedbackTemplate.Question(
                        "q1",
                        1,
                        VoteComponentType.MULTI_SELECT,
                        "만족한 요소",
                        null,
                        true,
                        2,
                        null,
                        List.of(
                                new FeedbackTemplate.Option("o1", "속도"),
                                new FeedbackTemplate.Option("o2", "안정성")),
                        null);
        final Vote vote = voteWith(new FeedbackTemplate("피드백", null, List.of(question)));
        final List<FeedbackOptionTally> options = List.of(new FeedbackOptionTally("q1", "o1", 4L));

        final FeedbackResultResponse response =
                FeedbackResultResponse.of(vote, options, List.of(), List.of(), 4L);

        final FeedbackResultResponse.QuestionResult result = response.questions().get(0);
        assertThat(result.type()).isEqualTo(VoteComponentType.MULTI_SELECT);
        assertThat(result.options())
                .extracting(FeedbackResultResponse.OptionResult::optionId)
                .containsExactly("o1", "o2");
        assertThat(result.options().get(0).count()).isEqualTo(4L);
        assertThat(result.options().get(1).count()).isZero();
        assertThat(result.trueCount()).isNull();
        assertThat(result.textAnswers()).isNull();
    }

    @Test
    @DisplayName("BOOLEAN 은 예/아니오 응답 수를 각각 집계한다")
    void of_boolean_countsTrueAndFalse() {
        final FeedbackTemplate.Question question =
                new FeedbackTemplate.Question(
                        "q1",
                        1,
                        VoteComponentType.BOOLEAN,
                        "추천 여부",
                        null,
                        false,
                        null,
                        null,
                        null,
                        null);
        final Vote vote = voteWith(new FeedbackTemplate("피드백", null, List.of(question)));
        final List<FeedbackBoolTally> bools =
                List.of(
                        new FeedbackBoolTally("q1", Boolean.TRUE, 7L),
                        new FeedbackBoolTally("q1", Boolean.FALSE, 3L));

        final FeedbackResultResponse response =
                FeedbackResultResponse.of(vote, List.of(), bools, List.of(), 10L);

        final FeedbackResultResponse.QuestionResult result = response.questions().get(0);
        assertThat(result.trueCount()).isEqualTo(7L);
        assertThat(result.falseCount()).isEqualTo(3L);
        assertThat(result.options()).isNull();
    }

    @Test
    @DisplayName("LONG_TEXT 는 작성된 텍스트 목록을 반환하고 followUp 질문도 평탄화해 함께 집계한다")
    void of_longText_andFollowUpFlattened() {
        final FeedbackTemplate.Question followUp =
                new FeedbackTemplate.Question(
                        "q1f",
                        2,
                        VoteComponentType.LONG_TEXT,
                        "그 이유는?",
                        null,
                        false,
                        null,
                        500,
                        null,
                        null);
        final FeedbackTemplate.Question question =
                new FeedbackTemplate.Question(
                        "q1",
                        1,
                        VoteComponentType.BOOLEAN,
                        "추천 여부",
                        null,
                        false,
                        null,
                        null,
                        null,
                        followUp);
        final Vote vote = voteWith(new FeedbackTemplate("피드백", null, List.of(question)));
        final List<FeedbackTextView> texts =
                List.of(new FeedbackTextView("q1f", "좋았어요"), new FeedbackTextView("q1f", "최고였습니다"));
        final List<FeedbackBoolTally> bools =
                List.of(new FeedbackBoolTally("q1", Boolean.TRUE, 5L));

        final FeedbackResultResponse response =
                FeedbackResultResponse.of(vote, List.of(), bools, texts, 5L);

        assertThat(response.questions())
                .extracting(FeedbackResultResponse.QuestionResult::questionId)
                .containsExactly("q1", "q1f");
        final FeedbackResultResponse.QuestionResult followUpResult = response.questions().get(1);
        assertThat(followUpResult.type()).isEqualTo(VoteComponentType.LONG_TEXT);
        assertThat(followUpResult.textAnswers()).containsExactly("좋았어요", "최고였습니다");
    }

    @Test
    @DisplayName("피드백 템플릿이 없으면 빈 질문 목록을 반환한다")
    void of_whenNoTemplate_returnsEmptyQuestions() {
        final Vote vote = voteWith(null);

        final FeedbackResultResponse response =
                FeedbackResultResponse.of(vote, List.of(), List.of(), List.of(), 0L);

        assertThat(response.questions()).isEmpty();
    }
}
