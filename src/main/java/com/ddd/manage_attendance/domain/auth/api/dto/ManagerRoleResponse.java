package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.ManagerRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[인증] 매니저 업무 응답 DTO")
public record ManagerRoleResponse(String key, String description) {
    public static ManagerRoleResponse from(ManagerRole managerRole) {
        return new ManagerRoleResponse(managerRole.name(), managerRole.getDescription());
    }
}
