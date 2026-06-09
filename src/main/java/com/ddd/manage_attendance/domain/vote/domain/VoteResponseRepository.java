package com.ddd.manage_attendance.domain.vote.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteResponseRepository extends JpaRepository<VoteResponse, Long> {

    Optional<VoteResponse> findByVoteIdAndMemberId(Long voteId, Long memberId);

    boolean existsByVoteIdAndMemberId(Long voteId, Long memberId);

    /** 특정 투표의 모든 응답 행. 참여 현황/미참여 명단 집계에 사용한다. */
    List<VoteResponse> findAllByVoteId(Long voteId);
}
