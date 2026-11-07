package com.ddd.manage_attendance.domain.vote.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.core.sdui.VoteComponentType;
import com.ddd.manage_attendance.domain.vote.exception.VoteTemplateStructureChangedException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VoteTemplateStructureTest {

    private static final TeamVoteTemplate.Category CATEGORY =
            new TeamVoteTemplate.Category("c1", 1, "분야", 2, false, 0, 10, "이유");
    private static final FeedbackTemplate.Option OPTION = new FeedbackTemplate.Option("o1", "선택지");

    @Test
    @DisplayName("부문 ID 가 바뀌면 구조 변경으로 거부한다")
    void rejectsCategoryIdChange() {
        rejects(
                team(List.of(category("c2", 1, 2, false, 0, 10))), null,
                team(List.of(CATEGORY)), null);
    }

    @Test
    @DisplayName("부문 순서가 바뀌면 구조 변경으로 거부한다")
    void rejectsCategoryOrderChange() {
        rejects(
                team(List.of(category("c1", 2, 2, false, 0, 10))), null,
                team(List.of(CATEGORY)), null);
    }

    @Test
    @DisplayName("부문이 추가되면 구조 변경으로 거부한다")
    void rejectsCategoryAdded() {
        rejects(
                team(List.of(CATEGORY, category("c2", 2, 2, false, 0, 10))), null,
                team(List.of(CATEGORY)), null);
    }

    @Test
    @DisplayName("부문이 삭제되면 구조 변경으로 거부한다")
    void rejectsCategoryRemoved() {
        rejects(
                team(List.of(CATEGORY)), null,
                team(List.of(CATEGORY, category("c2", 2, 2, false, 0, 10))), null);
    }

    @Test
    @DisplayName("부문별 최대 선택 수가 바뀌면 구조 변경으로 거부한다")
    void rejectsMaxSelectableTeamsChange() {
        rejects(
                team(List.of(category("c1", 1, 3, false, 0, 10))), null,
                team(List.of(CATEGORY)), null);
    }

    @Test
    @DisplayName("사유 필수 여부가 바뀌면 구조 변경으로 거부한다")
    void rejectsReasonRequiredChange() {
        rejects(
                team(List.of(category("c1", 1, 2, true, 0, 10))), null,
                team(List.of(CATEGORY)), null);
    }

    @Test
    @DisplayName("사유 최소 길이가 바뀌면 구조 변경으로 거부한다")
    void rejectsReasonMinLengthChange() {
        rejects(
                team(List.of(category("c1", 1, 2, false, 1, 10))), null,
                team(List.of(CATEGORY)), null);
    }

    @Test
    @DisplayName("사유 최대 길이가 바뀌면 구조 변경으로 거부한다")
    void rejectsReasonMaxLengthChange() {
        rejects(
                team(List.of(category("c1", 1, 2, false, 0, 11))), null,
                team(List.of(CATEGORY)), null);
    }

    @Test
    @DisplayName("질문 유형이 바뀌면 구조 변경으로 거부한다")
    void rejectsQuestionTypeChange() {
        rejects(
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.LONG_TEXT,
                                true,
                                2,
                                100,
                                List.of(),
                                null)),
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                100,
                                List.of(OPTION),
                                null)));
    }

    @Test
    @DisplayName("질문 필수 여부가 바뀌면 구조 변경으로 거부한다")
    void rejectsRequiredChange() {
        rejects(
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                false,
                                2,
                                null,
                                List.of(OPTION),
                                null)),
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                null)));
    }

    @Test
    @DisplayName("최대 선택지 수가 바뀌면 구조 변경으로 거부한다")
    void rejectsMaxSelectableOptionsChange() {
        rejects(
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                3,
                                null,
                                List.of(OPTION),
                                null)),
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                null)));
    }

    @Test
    @DisplayName("질문 최대 길이가 바뀌면 구조 변경으로 거부한다")
    void rejectsMaxLengthChange() {
        rejects(
                null,
                feedback(
                        question(
                                "q1", 1, VoteComponentType.LONG_TEXT, true, null, 101, null, null)),
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.LONG_TEXT,
                                true,
                                null,
                                100,
                                null,
                                null)));
    }

    @Test
    @DisplayName("선택지가 추가되면 구조 변경으로 거부한다")
    void rejectsOptionAdded() {
        rejects(
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION, new FeedbackTemplate.Option("o2", "두번째")),
                                null)),
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                null)));
    }

    @Test
    @DisplayName("선택지가 삭제되면 구조 변경으로 거부한다")
    void rejectsOptionRemoved() {
        rejects(
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                null)),
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION, new FeedbackTemplate.Option("o2", "두번째")),
                                null)));
    }

    @Test
    @DisplayName("선택지 순서가 바뀌면 구조 변경으로 거부한다")
    void rejectsOptionReorder() {
        final List<FeedbackTemplate.Option> options =
                List.of(OPTION, new FeedbackTemplate.Option("o2", "두번째"));
        rejects(
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(options.get(1), options.get(0)),
                                null)),
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                options,
                                null)));
    }

    @Test
    @DisplayName("후속 질문이 추가되면 구조 변경으로 거부한다")
    void rejectsFollowUpAdded() {
        rejects(
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                question(
                                        "q2",
                                        2,
                                        VoteComponentType.LONG_TEXT,
                                        false,
                                        null,
                                        100,
                                        null,
                                        null))),
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                null)));
    }

    @Test
    @DisplayName("후속 질문이 삭제되면 구조 변경으로 거부한다")
    void rejectsFollowUpRemoved() {
        rejects(
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                null)),
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                question(
                                        "q2",
                                        2,
                                        VoteComponentType.LONG_TEXT,
                                        false,
                                        null,
                                        100,
                                        null,
                                        null))));
    }

    @Test
    @DisplayName("후속 질문 구조가 바뀌면 구조 변경으로 거부한다")
    void rejectsFollowUpStructureChange() {
        rejects(
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                question(
                                        "q2",
                                        2,
                                        VoteComponentType.LONG_TEXT,
                                        false,
                                        null,
                                        100,
                                        null,
                                        null))),
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                question(
                                        "q2",
                                        2,
                                        VoteComponentType.BOOLEAN,
                                        false,
                                        null,
                                        null,
                                        null,
                                        null))));
    }

    @Test
    @DisplayName("팀 투표 템플릿이 null 에서 추가되면 구조 변경으로 거부한다")
    void rejectsTeamTemplateAdded() {
        rejects(null, null, team(List.of(CATEGORY)), null);
    }

    @Test
    @DisplayName("팀 투표 템플릿이 삭제되면 구조 변경으로 거부한다")
    void rejectsTeamTemplateRemoved() {
        rejects(team(List.of(CATEGORY)), null, null, null);
    }

    @Test
    @DisplayName("피드백 템플릿이 null 에서 추가되면 구조 변경으로 거부한다")
    void rejectsFeedbackTemplateAdded() {
        rejects(
                null,
                null,
                null,
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.LONG_TEXT,
                                true,
                                null,
                                100,
                                null,
                                null)));
    }

    @Test
    @DisplayName("피드백 템플릿이 삭제되면 구조 변경으로 거부한다")
    void rejectsFeedbackTemplateRemoved() {
        rejects(
                null,
                feedback(
                        question(
                                "q1", 1, VoteComponentType.LONG_TEXT, true, null, 100, null, null)),
                null,
                null);
    }

    @Test
    @DisplayName("후속 질문 내부 글자 변경은 허용한다")
    void allowsFollowUpTextChange() {
        final FeedbackTemplate oldTemplate =
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                question(
                                        "q2",
                                        2,
                                        VoteComponentType.LONG_TEXT,
                                        false,
                                        null,
                                        100,
                                        null,
                                        null)));
        final FeedbackTemplate newTemplate =
                feedback(
                        question(
                                "q1",
                                1,
                                VoteComponentType.MULTI_SELECT,
                                true,
                                2,
                                null,
                                List.of(OPTION),
                                new FeedbackTemplate.Question(
                                        "q2",
                                        2,
                                        VoteComponentType.LONG_TEXT,
                                        "새 후속 질문",
                                        "새 도움말",
                                        false,
                                        null,
                                        100,
                                        null,
                                        null)));

        assertThat(VoteTemplateStructure.sameStructure(oldTemplate, newTemplate)).isTrue();
    }

    @Test
    @DisplayName("notice, reasonLabel, helpText 변경은 허용한다")
    void allowsNoticeReasonLabelAndHelpTextChange() {
        final TeamVoteTemplate oldTeam = team(List.of(CATEGORY));
        final TeamVoteTemplate newTeam =
                new TeamVoteTemplate(
                        "새 제목",
                        "설명",
                        "새 공지",
                        List.of(
                                new TeamVoteTemplate.Category(
                                        "c1", 1, "새 분야", 2, false, 0, 10, "새 이유 라벨")));
        final FeedbackTemplate oldFeedback =
                feedback(
                        question(
                                "q1", 1, VoteComponentType.LONG_TEXT, true, null, 100, null, null));
        final FeedbackTemplate newFeedback =
                new FeedbackTemplate(
                        "새 피드백",
                        "설명",
                        List.of(
                                new FeedbackTemplate.Question(
                                        "q1",
                                        1,
                                        VoteComponentType.LONG_TEXT,
                                        "질문",
                                        "새 도움말",
                                        true,
                                        null,
                                        100,
                                        null,
                                        null)));

        assertThat(VoteTemplateStructure.sameStructure(oldTeam, newTeam)).isTrue();
        assertThat(VoteTemplateStructure.sameStructure(oldFeedback, newFeedback)).isTrue();
    }

    @Test
    @DisplayName("구조가 달라지면 assertTextOnlyChange 는 예외를 던진다")
    void assertTextOnlyChangeThrows() {
        assertThatThrownBy(
                        () ->
                                VoteTemplateStructure.assertTextOnlyChange(
                                        team(List.of(CATEGORY)),
                                        team(List.of(category("c2", 1, 2, false, 0, 10))),
                                        null,
                                        null))
                .isInstanceOf(VoteTemplateStructureChangedException.class);
    }

    private static void rejects(
            final TeamVoteTemplate oldTeam,
            final FeedbackTemplate oldFeedback,
            final TeamVoteTemplate newTeam,
            final FeedbackTemplate newFeedback) {
        assertThatThrownBy(
                        () ->
                                VoteTemplateStructure.assertTextOnlyChange(
                                        oldTeam, newTeam, oldFeedback, newFeedback))
                .isInstanceOf(VoteTemplateStructureChangedException.class);
    }

    private static TeamVoteTemplate team(final List<TeamVoteTemplate.Category> categories) {
        return new TeamVoteTemplate("제목", "설명", "공지", categories);
    }

    private static TeamVoteTemplate.Category category(
            final String id,
            final int order,
            final int maxTeams,
            final boolean reasonRequired,
            final int minLength,
            final int maxLength) {
        return new TeamVoteTemplate.Category(
                id, order, "분야", maxTeams, reasonRequired, minLength, maxLength, "이유");
    }

    private static FeedbackTemplate feedback(final FeedbackTemplate.Question question) {
        return new FeedbackTemplate("피드백", "설명", List.of(question));
    }

    private static FeedbackTemplate.Question question(
            final String id,
            final int order,
            final VoteComponentType type,
            final boolean required,
            final Integer maxOptions,
            final Integer maxLength,
            final List<FeedbackTemplate.Option> options,
            final FeedbackTemplate.Question followUp) {
        return new FeedbackTemplate.Question(
                id, order, type, "질문", "도움말", required, maxOptions, maxLength, options, followUp);
    }
}
