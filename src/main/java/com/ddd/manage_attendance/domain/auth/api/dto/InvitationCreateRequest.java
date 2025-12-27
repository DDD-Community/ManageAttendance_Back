package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.InvitationType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InvitationCreateRequest {

    private String code;
    private InvitationType type;
    private Long generationId;
    private String description;

    public InvitationCreateRequest(
            String code, InvitationType type, Long generationId, String description) {
        this.code = code;
        this.type = type;
        this.generationId = generationId;
        this.description = description;
    }
}
