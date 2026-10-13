package com.ddd.manage_attendance.domain.auth.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByCode(String code);

    boolean existsByGenerationId(Long generationId);

    boolean existsByCode(String code);

    List<Invitation> findAllByGenerationIdOrderByIdDesc(Long generationId);
}
