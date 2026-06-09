package com.ddd.manage_attendance.domain.vote.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    /** 한 기수에서 특정 상태(예: OPEN)인 가장 최근 투표를 조회한다. 기수당 진행 중 투표는 하나라는 전제. */
    Optional<Vote> findFirstByGenerationIdAndStatusOrderByIdDesc(
            Long generationId, VoteStatus status);
}
