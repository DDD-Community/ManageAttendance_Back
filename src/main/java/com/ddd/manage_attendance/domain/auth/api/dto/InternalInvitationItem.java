package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.InvitationType;
import java.time.LocalDateTime;

public record InternalInvitationItem(
        Long id,
        String code,
        InvitationType type,
        String description,
        long registeredCount,
        LocalDateTime createdAt) {}
