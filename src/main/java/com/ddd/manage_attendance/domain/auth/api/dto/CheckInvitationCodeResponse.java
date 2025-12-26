package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.Invitation;
import com.ddd.manage_attendance.domain.auth.domain.InvitationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[인증] 초대 코드 확인 응답 DTO")
public record CheckInvitationCodeResponse(
        Long generationId, String generationName, InvitationType type, String description) {

    public static CheckInvitationCodeResponse from(Invitation invitation) {
        return new CheckInvitationCodeResponse(
                invitation.getGeneration().getId(),
                invitation.getGeneration().getName(),
                invitation.getType(),
                invitation.getDescription());
    }
}
