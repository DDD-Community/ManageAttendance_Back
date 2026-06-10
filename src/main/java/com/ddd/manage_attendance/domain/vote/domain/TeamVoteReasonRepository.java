package com.ddd.manage_attendance.domain.vote.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamVoteReasonRepository extends JpaRepository<TeamVoteReason, Long> {

    List<TeamVoteReason> findByResponseId(Long responseId);

    /** 부문별 작성 사유 목록(익명). 결과 화면에서 운영진이 사유를 검토하는 데 사용한다. */
    @Query(
            """
            SELECT new com.ddd.manage_attendance.domain.vote.domain.TeamReasonView(
                t.categoryId, t.reason)
            FROM TeamVoteReason t
            JOIN VoteResponse r ON r.id = t.responseId
            WHERE r.voteId = :voteId AND t.reason IS NOT NULL
            ORDER BY t.id
""")
    List<TeamReasonView> findReasonsByVoteId(@Param("voteId") Long voteId);
}
