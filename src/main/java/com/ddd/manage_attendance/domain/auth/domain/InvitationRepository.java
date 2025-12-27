package com.ddd.manage_attendance.domain.auth.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "generation")
    Optional<Invitation> findByCode(String code);
}
