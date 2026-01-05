package com.ddd.manage_attendance.domain.auth.api.dto;

import com.ddd.manage_attendance.domain.auth.domain.JobRole;
import com.ddd.manage_attendance.domain.auth.domain.ManagerRole;
import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(title = "[유저] 회원가입 요청 DTO")
public record UserRegisterRequest(
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
        @Schema(description = "OAuth 제공자", example = "GOOGLE")
                @NotNull(message = "OAuth 제공자는 필수입니다.")
                OAuthProvider provider,
        @Schema(description = "인증 토큰", example = "eyJ...") @NotBlank(message = "인증 토큰은 필수입니다.")
                String token,
        @Schema(description = "OAuth Refresh Token (선택, 신규가입 시)", example = "r.123...")
                String oauthRefreshToken,
        @Schema(description = "초대 코드", example = "CODE123") @NotBlank(message = "초대 코드는 필수입니다.")
                String invitationCode) {}
