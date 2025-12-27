package com.ddd.manage_attendance.domain.team.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamCreateRequest {

    private String name;
    private Long generationId;

    public TeamCreateRequest(String name, Long generationId) {
        this.name = name;
        this.generationId = generationId;
    }
}
