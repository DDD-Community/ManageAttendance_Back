package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.InvitationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "내부 초대코드 생성 요청")
public record InternalInvitationCreateRequest(
        @Schema(description = "초대 유형", example = "MEMBER") @NotNull InvitationType type,
        @Schema(description = "초대 코드", example = "ABCD1234")
                @Pattern(regexp = "^[A-Za-z0-9_-]{4,50}$")
                String code,
        @Schema(description = "초대 설명", example = "내부 회원 초대") @Size(max = 255) String description) {}
