package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.InvitationType;

public record InvitationCreateRequest(
        String code, InvitationType type, Long generationId, String description) {}
