package com.ddd.manage_attendance.domain.vote.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackAnswerRepository extends JpaRepository<FeedbackAnswer, Long> {

    List<FeedbackAnswer> findByResponseId(Long responseId);

    /** MULTI_SELECT 선택지별 응답 수 집계. */
    @Query(
            """
            SELECT new com.ddd.manage_attendance.domain.vote.domain.FeedbackOptionTally(
                f.questionId, f.optionId, COUNT(f))
            FROM FeedbackAnswer f
            JOIN VoteResponse r ON r.id = f.responseId
            WHERE r.voteId = :voteId AND f.optionId IS NOT NULL
            GROUP BY f.questionId, f.optionId
""")
    List<FeedbackOptionTally> tallyOptionsByVoteId(@Param("voteId") Long voteId);

    /** BOOLEAN 예/아니오 응답 수 집계. */
    @Query(
            """
            SELECT new com.ddd.manage_attendance.domain.vote.domain.FeedbackBoolTally(
                f.questionId, f.boolValue, COUNT(f))
            FROM FeedbackAnswer f
            JOIN VoteResponse r ON r.id = f.responseId
            WHERE r.voteId = :voteId AND f.boolValue IS NOT NULL
            GROUP BY f.questionId, f.boolValue
""")
    List<FeedbackBoolTally> tallyBoolByVoteId(@Param("voteId") Long voteId);

    /** LONG_TEXT 작성 응답 목록(익명). */
    @Query(
            """
            SELECT new com.ddd.manage_attendance.domain.vote.domain.FeedbackTextView(
                f.questionId, f.textValue)
            FROM FeedbackAnswer f
            JOIN VoteResponse r ON r.id = f.responseId
            WHERE r.voteId = :voteId AND f.textValue IS NOT NULL
            ORDER BY f.id
""")
    List<FeedbackTextView> findTextsByVoteId(@Param("voteId") Long voteId);
}
