package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import com.ddd.manage_attendance.domain.vote.exception.VoteTemplateStructureChangedException;
import java.util.List;
import java.util.Objects;

/** 글자 필드를 제외한 투표 템플릿 구조가 같은지 비교한다. */
public final class VoteTemplateStructure {
    private VoteTemplateStructure() {}

    public static boolean sameStructure(final TeamVoteTemplate a, final TeamVoteTemplate b) {
        if (a == null || b == null) {
            return a == b;
        }
        return sameTeamCategories(a.categories(), b.categories());
    }

    private static boolean sameTeamCategories(
            final List<TeamVoteTemplate.Category> a, final List<TeamVoteTemplate.Category> b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            final TeamVoteTemplate.Category x = a.get(i);
            final TeamVoteTemplate.Category y = b.get(i);
            if (!Objects.equals(x.id(), y.id())
                    || x.order() != y.order()
                    || x.maxSelectableTeams() != y.maxSelectableTeams()
                    || x.reasonRequired() != y.reasonRequired()
                    || x.reasonMinLength() != y.reasonMinLength()
                    || x.reasonMaxLength() != y.reasonMaxLength()) {
                return false;
            }
        }
        return true;
    }

    public static boolean sameStructure(final FeedbackTemplate a, final FeedbackTemplate b) {
        if (a == null || b == null) {
            return a == b;
        }
        return sameQuestions(a.questions(), b.questions());
    }

    private static boolean sameQuestions(
            final List<FeedbackTemplate.Question> a, final List<FeedbackTemplate.Question> b) {
        if (a == null || b == null) return a == b;
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            final FeedbackTemplate.Question x = a.get(i);
            final FeedbackTemplate.Question y = b.get(i);
            if (!Objects.equals(x.id(), y.id())
                    || x.order() != y.order()
                    || x.type() != y.type()
                    || x.required() != y.required()
                    || !Objects.equals(x.maxSelectableOptions(), y.maxSelectableOptions())
                    || !Objects.equals(x.maxLength(), y.maxLength())
                    || !sameOptions(x.options(), y.options())
                    || !sameQuestion(x.followUp(), y.followUp())) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameQuestion(
            final FeedbackTemplate.Question a, final FeedbackTemplate.Question b) {
        return a == null ? b == null : b != null && sameQuestions(List.of(a), List.of(b));
    }

    private static boolean sameOptions(
            final List<FeedbackTemplate.Option> a, final List<FeedbackTemplate.Option> b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!Objects.equals(a.get(i).id(), b.get(i).id())) {
                return false;
            }
        }
        return true;
    }

    public static void assertTextOnlyChange(
            final TeamVoteTemplate oldTeam,
            final TeamVoteTemplate newTeam,
            final FeedbackTemplate oldFeedback,
            final FeedbackTemplate newFeedback) {
        if (!sameStructure(oldTeam, newTeam) || !sameStructure(oldFeedback, newFeedback)) {
            throw new VoteTemplateStructureChangedException();
        }
    }
}
