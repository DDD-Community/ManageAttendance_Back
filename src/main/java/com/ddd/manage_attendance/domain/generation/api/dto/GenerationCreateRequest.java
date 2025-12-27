package com.ddd.manage_attendance.domain.generation.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GenerationCreateRequest {

    private String name;

    public GenerationCreateRequest(String name) {
        this.name = name;
    }
}
