package com.ddd.manage_attendance.domain.vote.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteResponseRepository extends JpaRepository<VoteResponse, Long> {

    Optional<VoteResponse> findByVoteIdAndMemberId(Long voteId, Long memberId);

    boolean existsByVoteIdAndMemberId(Long voteId, Long memberId);
}
