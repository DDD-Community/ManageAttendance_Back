package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.JobRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(title = "[유저] 회원가입 요청 DTO")
public record UserRegisterRequest(
        @Schema(description = "이름", example = "홍길동") @NotBlank(message = "이름은 필수입니다.") String name,
        @Schema(description = "기수 ID", example = "1") @NotNull(message = "기수 ID는 필수입니다.")
                Long generationId,
        @Schema(description = "직군", example = "BACKEND") @NotNull(message = "직군은 필수입니다.")
                JobRole jobRole,
        @Schema(description = "팀 ID", example = "1") @NotNull(message = "팀 ID는 필수입니다.")
                Long teamId) {}
