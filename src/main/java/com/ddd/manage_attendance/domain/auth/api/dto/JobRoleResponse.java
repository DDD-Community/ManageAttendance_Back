package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.JobRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[인증] 직군 응답 DTO")
public record JobRoleResponse(String key, String description) {
    public static JobRoleResponse from(JobRole jobRole) {
        return new JobRoleResponse(jobRole.name(), jobRole.getDescription());
    }
}
