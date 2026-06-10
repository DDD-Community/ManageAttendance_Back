package com.ddd.manage_attendance.domain.vote.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamVoteAnswerRepository extends JpaRepository<TeamVoteAnswer, Long> {

    List<TeamVoteAnswer> findByResponseId(Long responseId);

    /** 부문별 팀 득표 집계. response_id 로 {@link VoteResponse} 와 조인해 해당 투표의 응답만 센다. */
    @Query(
            """
            SELECT new com.ddd.manage_attendance.domain.vote.domain.TeamVoteTally(
                a.categoryId, a.teamId, COUNT(a))
            FROM TeamVoteAnswer a
            JOIN VoteResponse r ON r.id = a.responseId
            WHERE r.voteId = :voteId
            GROUP BY a.categoryId, a.teamId
""")
    List<TeamVoteTally> tallyByVoteId(@Param("voteId") Long voteId);
}
