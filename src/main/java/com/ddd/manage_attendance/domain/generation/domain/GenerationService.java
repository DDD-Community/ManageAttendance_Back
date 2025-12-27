package com.ddd.manage_attendance.domain.generation.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerationService {
    private final GenerationRepository generationRepository;

    @Transactional(readOnly = true)
    public Generation findById(final Long generationId) {
        return generationRepository.findById(generationId).orElseThrow(DataNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public String getGenerationName(final Long generationId) {
        if (generationId == null) {
            return null;
        }
        return generationRepository.findById(generationId).map(Generation::getName).orElse(null);
    }

    @Transactional
    public Long createGeneration(String name) {
        Generation generation = Generation.builder().name(name).build();
        return generationRepository.save(generation).getId();
    }
}
