package com.ddd.manage_attendance.domain.vote.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.core.sdui.VoteComponentType;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest.FeedbackQuestionAnswer;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest.TeamVoteCategoryAnswer;
import com.ddd.manage_attendance.domain.vote.exception.OwnTeamSelectedException;
import com.ddd.manage_attendance.domain.vote.exception.VoteAnswerInvalidException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VoteAnswerValidatorTest {

    private static final Long OWN_TEAM_ID = 10L;
    private static final Set<Long> VALID_TEAM_IDS = Set.of(10L, 11L, 12L, 13L);

    private final VoteAnswerValidator validator = new VoteAnswerValidator();

    private Vote voteWithTemplates() {
        final TeamVoteTemplate teamVoteTemplate =
                new TeamVoteTemplate(
                        "팀 투표",
                        "설명",
                        "본인 팀 제외",
                        List.of(
                                new TeamVoteTemplate.Category(
                                        "PLANNING", 1, "기획", 2, true, 5, 300, "사유")));
        final FeedbackTemplate feedbackTemplate =
                new FeedbackTemplate(
                        "피드백",
                        "설명",
                        List.of(
                                new FeedbackTemplate.Question(
                                        "BEST",
                                        1,
                                        VoteComponentType.MULTI_SELECT,
                                        "가장 만족한 커리큘럼",
                                        "모두 선택 가능",
                                        true,
                                        2,
                                        null,
                                        List.of(
                                                new FeedbackTemplate.Option("OT", "OT"),
                                                new FeedbackTemplate.Option("FINAL", "최종 발표")),
                                        new FeedbackTemplate.Question(
                                                "BEST_REASON",
                                                2,
                                                VoteComponentType.LONG_TEXT,
                                                "어떤 부분이 좋았나요?",
                                                null,
                                                false,
                                                null,
                                                300,
                                                null,
                                                null))));
        return Vote.createDraft(1L, "투표", teamVoteTemplate, feedbackTemplate);
    }

    private VoteSubmitRequest request(
            final List<TeamVoteCategoryAnswer> teamVote,
            final List<FeedbackQuestionAnswer> feedback) {
        return new VoteSubmitRequest(teamVote, feedback);
    }

    // TOP -> L1 -> L2 로 follow-up 이 2단계 중첩된 피드백 템플릿
    private Vote voteWithNestedFollowUp() {
        final FeedbackTemplate.Question deep =
                new FeedbackTemplate.Question(
                        "L2",
                        3,
                        VoteComponentType.LONG_TEXT,
                        "심화",
                        null,
                        false,
                        null,
                        300,
                        null,
                        null);
        final FeedbackTemplate.Question mid =
                new FeedbackTemplate.Question(
                        "L1",
                        2,
                        VoteComponentType.LONG_TEXT,
                        "후속",
                        null,
                        false,
                        null,
                        300,
                        null,
                        deep);
        final FeedbackTemplate.Question top =
                new FeedbackTemplate.Question(
                        "TOP",
                        1,
                        VoteComponentType.LONG_TEXT,
                        "최상위",
                        null,
                        false,
                        null,
                        300,
                        null,
                        mid);
        return Vote.createDraft(1L, "투표", null, new FeedbackTemplate("피드백", "설명", List.of(top)));
    }

    @Test
    @DisplayName("타입과 맞지 않는 필드를 함께 보내면 예외가 발생한다")
    void mixedTypeFields_throws() {
        final VoteSubmitRequest request =
                request(
                        List.of(),
                        List.of(
                                new FeedbackQuestionAnswer(
                                        "BEST", List.of("OT"), "엉뚱한 텍스트", null)));

        assertThatThrownBy(
                        () ->
                                validator.validate(
                                        voteWithTemplates(), OWN_TEAM_ID, VALID_TEAM_IDS, request))
                .isInstanceOf(VoteAnswerInvalidException.class);
    }

    @Test
    @DisplayName("2단계 이상 중첩된 follow-up 질문 응답도 인식한다")
    void deeplyNestedFollowUp_isRecognized() {
        final VoteSubmitRequest request =
                request(List.of(), List.of(new FeedbackQuestionAnswer("L2", null, "심화 응답", null)));

        assertThatCode(
                        () ->
                                validator.validate(
                                        voteWithNestedFollowUp(),
                                        OWN_TEAM_ID,
                                        VALID_TEAM_IDS,
                                        request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("정상 응답은 통과한다")
    void validSubmission_passes() {
        final VoteSubmitRequest request =
                request(
                        List.of(
                                new TeamVoteCategoryAnswer(
                                        "PLANNING", List.of(11L, 12L), "기획이 탄탄했어요")),
                        List.of(new FeedbackQuestionAnswer("BEST", List.of("OT"), null, null)));

        assertThatCode(
                        () ->
                                validator.validate(
                                        voteWithTemplates(), OWN_TEAM_ID, VALID_TEAM_IDS, request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("본인 팀을 선택하면 예외가 발생한다")
    void ownTeamSelected_throws() {
        final VoteSubmitRequest request =
                request(
                        List.of(
                                new TeamVoteCategoryAnswer(
                                        "PLANNING", List.of(OWN_TEAM_ID), "사유입니다")),
                        List.of(new FeedbackQuestionAnswer("BEST", List.of("OT"), null, null)));

        assertThatThrownBy(
                        () ->
                                validator.validate(
                                        voteWithTemplates(), OWN_TEAM_ID, VALID_TEAM_IDS, request))
                .isInstanceOf(OwnTeamSelectedException.class);
    }

    @Test
    @DisplayName("부문 최대 선택 팀 수를 초과하면 예외가 발생한다")
    void exceedMaxTeams_throws() {
        final VoteSubmitRequest request =
                request(
                        List.of(
                                new TeamVoteCategoryAnswer(
                                        "PLANNING", List.of(11L, 12L, 13L), "사유입니다")),
                        List.of(new FeedbackQuestionAnswer("BEST", List.of("OT"), null, null)));

        assertThatThrownBy(
                        () ->
                                validator.validate(
                                        voteWithTemplates(), OWN_TEAM_ID, VALID_TEAM_IDS, request))
                .isInstanceOf(VoteAnswerInvalidException.class);
    }

    @Test
    @DisplayName("사유가 최소 글자수보다 짧으면 예외가 발생한다")
    void reasonTooShort_throws() {
        final VoteSubmitRequest request =
                request(
                        List.of(new TeamVoteCategoryAnswer("PLANNING", List.of(11L), "짧음")),
                        List.of(new FeedbackQuestionAnswer("BEST", List.of("OT"), null, null)));

        assertThatThrownBy(
                        () ->
                                validator.validate(
                                        voteWithTemplates(), OWN_TEAM_ID, VALID_TEAM_IDS, request))
                .isInstanceOf(VoteAnswerInvalidException.class);
    }

    @Test
    @DisplayName("필수 피드백 질문에 응답하지 않으면 예외가 발생한다")
    void requiredFeedbackMissing_throws() {
        final VoteSubmitRequest request =
                request(
                        List.of(new TeamVoteCategoryAnswer("PLANNING", List.of(11L), "기획이 탄탄했어요")),
                        List.of());

        assertThatThrownBy(
                        () ->
                                validator.validate(
                                        voteWithTemplates(), OWN_TEAM_ID, VALID_TEAM_IDS, request))
                .isInstanceOf(VoteAnswerInvalidException.class);
    }

    @Test
    @DisplayName("팀 투표 부문에 팀을 선택하지 않으면 예외가 발생한다")
    void teamCategoryNotSelected_throws() {
        final VoteSubmitRequest request =
                request(
                        List.of(),
                        List.of(new FeedbackQuestionAnswer("BEST", List.of("OT"), null, null)));

        assertThatThrownBy(
                        () ->
                                validator.validate(
                                        voteWithTemplates(), OWN_TEAM_ID, VALID_TEAM_IDS, request))
                .isInstanceOf(VoteAnswerInvalidException.class);
    }

    @Test
    @DisplayName("다중 선택에서 같은 선택지를 중복 선택하면 예외가 발생한다")
    void duplicateMultiSelect_throws() {
        final VoteSubmitRequest request =
                request(
                        List.of(),
                        List.of(
                                new FeedbackQuestionAnswer(
                                        "BEST", List.of("OT", "OT"), null, null)));

        assertThatThrownBy(
                        () ->
                                validator.validate(
                                        voteWithTemplates(), OWN_TEAM_ID, VALID_TEAM_IDS, request))
                .isInstanceOf(VoteAnswerInvalidException.class);
    }

    @Test
    @DisplayName("존재하지 않는 팀을 선택하면 예외가 발생한다")
    void unknownTeam_throws() {
        final VoteSubmitRequest request =
                request(
                        List.of(new TeamVoteCategoryAnswer("PLANNING", List.of(999L), "사유입니다")),
                        List.of(new FeedbackQuestionAnswer("BEST", List.of("OT"), null, null)));

        assertThatThrownBy(
                        () ->
                                validator.validate(
                                        voteWithTemplates(), OWN_TEAM_ID, VALID_TEAM_IDS, request))
                .isInstanceOf(VoteAnswerInvalidException.class);
    }
}
