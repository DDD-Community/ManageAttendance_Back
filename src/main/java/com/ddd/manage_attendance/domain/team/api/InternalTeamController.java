package com.ddd.manage_attendance.domain.team.api;

import com.ddd.manage_attendance.domain.team.api.dto.InternalTeamCreateRequest;
import com.ddd.manage_attendance.domain.team.api.dto.InternalTeamItem;
import com.ddd.manage_attendance.domain.team.domain.InternalTeamFacade;
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
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Tag(name = "내부 팀 API", description = "내부 시스템에서 기수별 팀을 조회하고 관리합니다.")
@SecurityRequirement(name = "InternalApiKey")
public class InternalTeamController {
    private final InternalTeamFacade facade;

    @GetMapping("/generations/{generationId}/teams")
    @Operation(summary = "[내부] 기수별 팀 목록 조회", description = "기수에 등록된 팀과 회원 수를 조회합니다.")
    public List<InternalTeamItem> list(@PathVariable @Positive final Long generationId) {
        return facade.listTeams(generationId);
    }

    @PostMapping("/generations/{generationId}/teams")
    @Operation(summary = "[내부] 팀 생성", description = "기수에 팀을 생성합니다. 기수가 없으면 404를 반환합니다.")
    public ResponseEntity<InternalTeamItem> create(
            @PathVariable @Positive final Long generationId,
            @Valid @RequestBody final InternalTeamCreateRequest request) {
        final InternalTeamItem result =
                facade.createTeam(generationId, request.name(), request.serviceName());
        return ResponseEntity.created(URI.create("/api/internal/teams/" + result.id()))
                .body(result);
    }

    @DeleteMapping("/teams/{teamId}")
    @Operation(summary = "[내부] 팀 삭제", description = "팀을 삭제합니다. 팀이 없으면 404, 회원이 참조 중이면 409를 반환합니다.")
    public ResponseEntity<Void> delete(@PathVariable @Positive final Long teamId) {
        facade.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }
}
