package com.ddd.manage_attendance.domain.team.api;

import com.ddd.manage_attendance.domain.team.api.dto.TeamCreateRequest;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
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
@RequestMapping("/api/team")
@RequiredArgsConstructor
@Tag(name = "Team", description = "팀 API")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "팀 생성", description = "새로운 팀을 생성합니다.")
    public ResponseEntity<Long> createTeam(@Valid @RequestBody final TeamCreateRequest request) {
        return ResponseEntity.ok(teamService.createTeam(request.name(), request.generationId()));
    }
}
