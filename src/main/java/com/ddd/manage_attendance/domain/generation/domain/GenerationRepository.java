package com.ddd.manage_attendance.domain.generation.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationRepository extends JpaRepository<Generation, Long> {
    boolean existsByName(String name);

    List<Generation> findAllByOrderByIdDesc();
}
