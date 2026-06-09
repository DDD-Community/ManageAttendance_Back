package com.ddd.manage_attendance.domain.vote.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackAnswerRepository extends JpaRepository<FeedbackAnswer, Long> {

    List<FeedbackAnswer> findByResponseId(Long responseId);
}
