package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.InternalInvitationCreateRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.InternalInvitationItem;
import com.ddd.manage_attendance.domain.auth.domain.InternalInvitationFacade;
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
@Tag(name = "내부 초대 API", description = "내부 시스템에서 기수 초대코드를 관리합니다.")
@SecurityRequirement(name = "InternalApiKey")
public class InternalInvitationController {
    private final InternalInvitationFacade facade;

    @GetMapping("/generations/{generationId}/invitations")
    @Operation(summary = "[내부] 기수별 초대코드 목록 조회", description = "기수의 초대코드 목록을 조회합니다.")
    public List<InternalInvitationItem> list(@PathVariable @Positive final Long generationId) {
        return facade.listInvitations(generationId);
    }

    @PostMapping("/generations/{generationId}/invitations")
    @Operation(
            summary = "[내부] 초대코드 생성",
            description = "기수 초대코드를 생성합니다. 기수가 없거나 코드가 중복되면 404 또는 409를 반환합니다.")
    public ResponseEntity<InternalInvitationItem> create(
            @PathVariable @Positive final Long generationId,
            @Valid @RequestBody final InternalInvitationCreateRequest request) {
        final InternalInvitationItem result =
                facade.createInvitation(
                        generationId, request.type(), request.code(), request.description());
        return ResponseEntity.created(URI.create("/api/internal/invitations/" + result.id()))
                .body(result);
    }

    @DeleteMapping("/invitations/{invitationId}")
    @Operation(summary = "[내부] 초대코드 삭제", description = "초대코드를 삭제합니다. 초대코드가 없으면 404를 반환합니다.")
    public ResponseEntity<Void> delete(@PathVariable @Positive final Long invitationId) {
        facade.deleteInvitation(invitationId);
        return ResponseEntity.noContent().build();
    }
}
