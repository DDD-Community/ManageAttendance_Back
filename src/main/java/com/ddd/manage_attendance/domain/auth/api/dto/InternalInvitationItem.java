package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.InvitationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "내부 초대코드 항목")
public record InternalInvitationItem(
        @Schema(description = "초대코드 ID", example = "1") Long id,
        @Schema(description = "초대 코드", example = "ABCD1234") String code,
        @Schema(description = "초대 유형", example = "MEMBER") InvitationType type,
        @Schema(description = "초대 설명", example = "내부 회원 초대") String description,
        @Schema(description = "등록 회원 수", example = "0") long registeredCount,
        @Schema(description = "생성 시각", example = "2026-10-12T10:00:00") LocalDateTime createdAt) {}
