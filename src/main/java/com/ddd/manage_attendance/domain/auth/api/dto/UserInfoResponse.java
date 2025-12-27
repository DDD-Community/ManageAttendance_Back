package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.JobRole;
import com.ddd.manage_attendance.domain.auth.domain.ManagerRole;
import com.ddd.manage_attendance.domain.auth.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(title = "[유저] 내 정보 응답 DTO")
public record UserInfoResponse(
        @Schema(description = "사용자 ID", example = "1") Long userId,
        @Schema(description = "사용자 이름", example = "홍길동") String name,
        @Schema(description = "이메일", example = "user@example.com") String email,
        @Schema(description = "기수명", example = "13기") String generation,
        @Schema(description = "팀명", example = "web1팀") String team,
        @Schema(description = "직군") JobRole jobRole,
        @Schema(description = "매니저 역할 목록") @JsonInclude(JsonInclude.Include.NON_NULL)
                List<ManagerRole> managerRoles) {

    public static UserInfoResponse from(User user, String generationName, String teamName) {
        return new UserInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                generationName,
                teamName,
                user.getJob(),
                user.getManagerRolesOrNull());
    }
}
