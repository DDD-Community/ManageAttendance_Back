package com.ddd.manage_attendance.domain.generation.api;

import com.ddd.manage_attendance.domain.generation.api.dto.GenerationCreateRequest;
import com.ddd.manage_attendance.domain.generation.domain.GenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generation")
@RequiredArgsConstructor
@Tag(name = "Generation", description = "기수 API")
public class GenerationController {

    private final GenerationService generationService;

    @PostMapping
    @Operation(summary = "기수 생성", description = "새로운 기수를 생성합니다.")
    public ResponseEntity<Long> createGeneration(
            @Valid @RequestBody final GenerationCreateRequest request) {
        return ResponseEntity.ok(generationService.createGeneration(request.getName()));
    }
}
