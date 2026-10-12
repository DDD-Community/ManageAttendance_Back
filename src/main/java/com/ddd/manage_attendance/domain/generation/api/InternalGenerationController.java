package com.ddd.manage_attendance.domain.generation.api;

import com.ddd.manage_attendance.domain.generation.api.dto.InternalGenerationCreateRequest;
import com.ddd.manage_attendance.domain.generation.api.dto.InternalGenerationItem;
import com.ddd.manage_attendance.domain.generation.domain.InternalGenerationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/generations")
@RequiredArgsConstructor
@Tag(name = "내부 기수 API")
@SecurityRequirement(name = "InternalApiKey")
public class InternalGenerationController {
    private final InternalGenerationFacade facade;

    @GetMapping
    @Operation(summary = "[내부] 기수 목록 조회")
    public List<InternalGenerationItem> list() {
        return facade.listGenerations();
    }

    @PostMapping
    @Operation(summary = "[내부] 기수 생성")
    public ResponseEntity<InternalGenerationItem> create(
            @Valid @RequestBody final InternalGenerationCreateRequest request) {
        final InternalGenerationItem result = facade.createGeneration(request.name());
        return ResponseEntity.created(URI.create("/api/internal/generations/" + result.id()))
                .body(result);
    }

    @DeleteMapping("/{generationId}")
    @Operation(summary = "[내부] 기수 삭제")
    public ResponseEntity<Void> delete(@PathVariable @Positive final Long generationId) {
        facade.deleteGeneration(generationId);
        return ResponseEntity.noContent().build();
    }
}
