package com.ddd.manage_attendance.domain.team.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findAllByGenerationId(Long generationId);

    List<Team> findAllByGenerationIdOrderByNameAsc(Long generationId);

    boolean existsByGenerationId(Long generationId);

    boolean existsByNameAndGenerationId(String name, Long generationId);
}
