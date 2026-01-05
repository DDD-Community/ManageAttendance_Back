package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.Invitation;
import com.ddd.manage_attendance.domain.auth.domain.InvitationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[인증] 초대 코드 확인 응답 DTO")
public record CheckInvitationCodeResponse(
        Long generationId, String generationName, InvitationType type, String description) {

    public static CheckInvitationCodeResponse from(Invitation invitation, String generationName) {
        return new CheckInvitationCodeResponse(
                invitation.getGenerationId(),
                generationName,
                invitation.getType(),
                invitation.getDescription());
    }
}
