package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.CheckInvitationCodeResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.JobRoleResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.ManagerRoleResponse;
import com.ddd.manage_attendance.domain.auth.domain.Invitation;
import com.ddd.manage_attendance.domain.auth.domain.InvitationService;
import com.ddd.manage_attendance.domain.auth.domain.JobRole;
import com.ddd.manage_attendance.domain.auth.domain.ManagerRole;
import com.ddd.manage_attendance.domain.generation.domain.GenerationService;
import com.ddd.manage_attendance.domain.team.api.dto.TeamResponse;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@Tag(name = "Onboarding", description = "온보딩(회원가입 절차) 관련 API - 모든 API 인증 불필요")
public class OnboardingController {

    private final InvitationService invitationService;
    private final TeamService teamService;
    private final GenerationService generationService;

    @GetMapping("/verify-code")
    @Operation(summary = "초대 코드 검증", description = "입력한 초대 코드가 유효한지 확인하고 기수 정보를 반환합니다.")
    public CheckInvitationCodeResponse verifyCode(@RequestParam String code) {
        Invitation invitation = invitationService.verifyCode(code);
        String generationName = generationService.getGenerationName(invitation.getGenerationId());
        return CheckInvitationCodeResponse.from(invitation, generationName);
    }

    @GetMapping("/jobs")
    @Operation(summary = "직군 목록 조회", description = "선택 가능한 직군 목록을 반환합니다.")
    public List<JobRoleResponse> getJobRoles() {
        return Arrays.stream(JobRole.values()).map(JobRoleResponse::from).toList();
    }

    @GetMapping("/teams")
    @Operation(summary = "팀 목록 조회", description = "특정 기수의 팀 목록을 반환합니다.")
    public List<TeamResponse> getTeams(@RequestParam Long generationId) {
        return TeamResponse.fromList(teamService.findAllByGenerationId(generationId));
    }

    @GetMapping("/manager-roles")
    @Operation(summary = "매니저 업무 목록 조회", description = "선택 가능한 매니저 업무 목록을 반환합니다.")
    public List<ManagerRoleResponse> getManagerRoles() {
        return Arrays.stream(ManagerRole.values()).map(ManagerRoleResponse::from).toList();
    }
}
