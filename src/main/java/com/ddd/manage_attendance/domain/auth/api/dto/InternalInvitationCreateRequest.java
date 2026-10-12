package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.InvitationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InternalInvitationCreateRequest(
        @NotNull InvitationType type,
        @Pattern(regexp = "^[A-Za-z0-9_-]{4,50}$") String code,
        @Size(max = 255) String description) {}
