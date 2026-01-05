package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.InvitationCreateRequest;
import com.ddd.manage_attendance.domain.auth.domain.InvitationService;
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
@RequestMapping("/api/invitation")
@RequiredArgsConstructor
@Tag(name = "Invitation", description = "초대코드 API")
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    @Operation(summary = "초대코드 생성", description = "새로운 초대코드를 생성합니다.")
    public ResponseEntity<Long> createInvitation(
            @Valid @RequestBody final InvitationCreateRequest request) {
        return ResponseEntity.ok(
                invitationService.createInvitation(
                        request.code(),
                        request.type(),
                        request.generationId(),
                        request.description()));
    }
}
