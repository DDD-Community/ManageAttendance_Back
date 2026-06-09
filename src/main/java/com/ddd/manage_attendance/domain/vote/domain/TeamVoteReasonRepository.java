package com.ddd.manage_attendance.domain.vote.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamVoteReasonRepository extends JpaRepository<TeamVoteReason, Long> {

    List<TeamVoteReason> findByResponseId(Long responseId);
}
