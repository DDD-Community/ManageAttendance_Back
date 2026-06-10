package com.ddd.manage_attendance.domain.vote.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteResponseRepository extends JpaRepository<VoteResponse, Long> {

    boolean existsByVoteIdAndMemberId(Long voteId, Long memberId);

    /** 특정 투표의 모든 응답 행. 참여 현황/미참여 명단 집계에 사용한다. */
    List<VoteResponse> findAllByVoteId(Long voteId);

    /** 특정 투표의 총 응답자 수. 결과 집계의 분모로 사용한다. */
    long countByVoteId(Long voteId);
}
