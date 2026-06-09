package com.ddd.manage_attendance.domain.vote.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamVoteAnswerRepository extends JpaRepository<TeamVoteAnswer, Long> {

    List<TeamVoteAnswer> findByResponseId(Long responseId);
}
