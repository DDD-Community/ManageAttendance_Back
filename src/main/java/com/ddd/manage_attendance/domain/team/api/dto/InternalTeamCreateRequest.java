package com.ddd.manage_attendance.domain.team.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InternalTeamCreateRequest(
        @NotBlank @Size(max = 30) String name, @Size(max = 100) String serviceName) {}
