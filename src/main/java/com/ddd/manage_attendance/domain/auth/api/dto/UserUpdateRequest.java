package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.JobRole;
import com.ddd.manage_attendance.domain.auth.domain.ManagerRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(title = "[유저] 정보 수정(재등록) 요청 DTO")
public record UserUpdateRequest(
        @Schema(description = "이름", example = "홍길동") @NotBlank(message = "이름은 필수입니다.") String name,
        @Schema(description = "기수 ID", example = "1") @NotNull(message = "기수 ID는 필수입니다.")
                Long generationId,
        @Schema(description = "직군", example = "BACKEND") @NotNull(message = "직군은 필수입니다.")
                JobRole jobRole,
        @Schema(description = "팀 ID (매니저는 null 가능)", example = "1") Long teamId,
        @Schema(
                        description = "매니저 업무 목록 (매니저인 경우 필수)",
                        example = "[\"TEAM_MANAGING\", \"ATTENDANCE_CHECK\"]")
                List<ManagerRole> managerRoles,
        @Schema(description = "초대 코드", example = "CODE123") @NotBlank(message = "초대 코드는 필수입니다.")
                String invitationCode) {}
