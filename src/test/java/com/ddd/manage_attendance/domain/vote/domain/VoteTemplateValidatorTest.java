package com.ddd.manage_attendance.domain.vote.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.core.sdui.VoteComponentType;
import com.ddd.manage_attendance.domain.vote.exception.VoteTemplateInvalidException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VoteTemplateValidatorTest {

    private final VoteTemplateValidator validator = new VoteTemplateValidator();

    private TeamVoteTemplate.Category category(
            final String id, final int maxTeams, final int minLen, final int maxLen) {
        return new TeamVoteTemplate.Category(id, 1, "부문", maxTeams, true, minLen, maxLen, "사유");
    }

    private FeedbackTemplate.Question question(
            final String id,
            final VoteComponentType type,
            final List<FeedbackTemplate.Option> opts) {
        return new FeedbackTemplate.Question(id, 1, type, "질문", null, true, null, 300, opts, null);
    }

    private TeamVoteTemplate teamTemplate(final List<TeamVoteTemplate.Category> categories) {
        return new TeamVoteTemplate("팀 투표", "설명", "공지", categories);
    }

    @Test
    @DisplayName("정상 템플릿은 통과한다")
    void valid_passes() {
        final TeamVoteTemplate team = teamTemplate(List.of(category("PLANNING", 2, 5, 300)));
        final FeedbackTemplate feedback =
                new FeedbackTemplate(
                        "피드백",
                        "설명",
                        List.of(
                                question(
                                        "BEST",
                                        VoteComponentType.MULTI_SELECT,
                                        List.of(new FeedbackTemplate.Option("OT", "OT")))));

        assertThatCode(() -> validator.validate(team, feedback)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("두 템플릿이 모두 없으면 예외가 발생한다")
    void bothNull_throws() {
        assertThatThrownBy(() -> validator.validate(null, null))
                .isInstanceOf(VoteTemplateInvalidException.class);
    }

    @Test
    @DisplayName("부문 ID가 중복되면 예외가 발생한다")
    void duplicateCategoryId_throws() {
        final TeamVoteTemplate team =
                teamTemplate(List.of(category("DUP", 2, 5, 300), category("DUP", 2, 5, 300)));

        assertThatThrownBy(() -> validator.validate(team, null))
                .isInstanceOf(VoteTemplateInvalidException.class);
    }

    @Test
    @DisplayName("최대 선택 팀 수가 1 미만이면 예외가 발생한다")
    void nonPositiveMaxTeams_throws() {
        final TeamVoteTemplate team = teamTemplate(List.of(category("PLANNING", 0, 5, 300)));

        assertThatThrownBy(() -> validator.validate(team, null))
                .isInstanceOf(VoteTemplateInvalidException.class);
    }

    @Test
    @DisplayName("사유 최소 글자수가 최대보다 크면 예외가 발생한다")
    void reasonMinGreaterThanMax_throws() {
        final TeamVoteTemplate team = teamTemplate(List.of(category("PLANNING", 2, 300, 5)));

        assertThatThrownBy(() -> validator.validate(team, null))
                .isInstanceOf(VoteTemplateInvalidException.class);
    }

    @Test
    @DisplayName("부문이 비어 있으면 예외가 발생한다")
    void emptyCategories_throws() {
        assertThatThrownBy(() -> validator.validate(teamTemplate(List.of()), null))
                .isInstanceOf(VoteTemplateInvalidException.class);
    }

    @Test
    @DisplayName("피드백 질문 타입이 TEAM_SELECT 이면 예외가 발생한다")
    void teamSelectFeedbackType_throws() {
        final FeedbackTemplate feedback =
                new FeedbackTemplate(
                        "피드백", "설명", List.of(question("Q", VoteComponentType.TEAM_SELECT, null)));

        assertThatThrownBy(() -> validator.validate(null, feedback))
                .isInstanceOf(VoteTemplateInvalidException.class);
    }

    @Test
    @DisplayName("다중 선택 질문에 선택지가 없으면 예외가 발생한다")
    void multiSelectWithoutOptions_throws() {
        final FeedbackTemplate feedback =
                new FeedbackTemplate(
                        "피드백",
                        "설명",
                        List.of(question("Q", VoteComponentType.MULTI_SELECT, List.of())));

        assertThatThrownBy(() -> validator.validate(null, feedback))
                .isInstanceOf(VoteTemplateInvalidException.class);
    }

    @Test
    @DisplayName("질문 ID가 (follow-up 포함) 중복되면 예외가 발생한다")
    void duplicateQuestionId_throws() {
        final FeedbackTemplate.Question followUp =
                new FeedbackTemplate.Question(
                        "DUP",
                        2,
                        VoteComponentType.LONG_TEXT,
                        "후속",
                        null,
                        false,
                        null,
                        300,
                        null,
                        null);
        final FeedbackTemplate.Question top =
                new FeedbackTemplate.Question(
                        "DUP",
                        1,
                        VoteComponentType.LONG_TEXT,
                        "최상위",
                        null,
                        false,
                        null,
                        300,
                        null,
                        followUp);
        final FeedbackTemplate feedback = new FeedbackTemplate("피드백", "설명", List.of(top));

        assertThatThrownBy(() -> validator.validate(null, feedback))
                .isInstanceOf(VoteTemplateInvalidException.class);
    }
}
