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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(
            summary = "초대 코드 검증",
            description =
                    "입력한 초대 코드가 유효한지 확인하고 기수 정보를 반환합니다.\n\n"
                            + "## 사용 시점\n"
                            + "회원가입 첫 단계에서 초대 코드를 입력받아 검증합니다.\n\n"
                            + "## 응답 정보\n"
                            + "- 기수 ID (generationId)\n"
                            + "- 기수 이름 (generationName)\n"
                            + "- 초대 코드 유효성 여부\n\n"
                            + "## 에러 케이스\n"
                            + "- 유효하지 않은 코드: `AUTH_INVALID_INVITATION_CODE` (400)\n"
                            + "- 만료된 코드: `AUTH_INVALID_INVITATION_CODE` (400)",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "초대 코드 검증 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = CheckInvitationCodeResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        value =
                                                                """
                                                {
                                                  "generationId": 1,
                                                  "generationName": "11기"
                                                }
                                                """)))
            })
    public CheckInvitationCodeResponse verifyCode(
            @Parameter(description = "초대 코드", example = "1234") @RequestParam String code) {
        Invitation invitation = invitationService.verifyCode(code);
        String generationName = generationService.getGenerationName(invitation.getGenerationId());
        return CheckInvitationCodeResponse.from(invitation, generationName);
    }

    @GetMapping("/jobs")
    @Operation(
            summary = "직군 목록 조회",
            description =
                    "선택 가능한 직군 목록을 반환합니다.\n\n"
                            + "## 사용 시점\n"
                            + "회원가입 시 사용자가 선택할 수 있는 직군 목록을 보여줍니다.\n\n"
                            + "## 직군 종류\n"
                            + "- BACKEND: 백엔드 개발자\n"
                            + "- FRONTEND: 프론트엔드 개발자\n"
                            + "- DESIGNER: 디자이너\n"
                            + "- PM: 프로젝트 매니저\n"
                            + "- IOS: iOS 개발자\n"
                            + "- ANDROID: Android 개발자\n"
                            + "- ETC: 기타",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "직군 목록 조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        value =
                                                                """
                                                [
                                                  {
                                                    "code": "BACKEND",
                                                    "name": "백엔드"
                                                  },
                                                  {
                                                    "code": "FRONTEND",
                                                    "name": "프론트엔드"
                                                  },
                                                  {
                                                    "code": "DESIGNER",
                                                    "name": "디자이너"
                                                  }
                                                ]
                                                """)))
            })
    public List<JobRoleResponse> getJobRoles() {
        return Arrays.stream(JobRole.values()).map(JobRoleResponse::from).toList();
    }

    @GetMapping("/teams")
    @Operation(
            summary = "팀 목록 조회",
            description =
                    "특정 기수의 팀 목록을 반환합니다.\n\n"
                            + "## 사용 시점\n"
                            + "회원가입 시 사용자가 소속될 팀을 선택합니다.\n"
                            + "초대 코드 검증에서 받은 generationId를 사용합니다.\n\n"
                            + "## 주의사항\n"
                            + "- 매니저는 팀 선택 생략 가능 (teamId null)\n"
                            + "- 일반 멤버는 팀 선택 필수\n"
                            + "- 존재하지 않는 기수 ID는 빈 배열 반환",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "팀 목록 조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        value =
                                                                """
                                                [
                                                  {
                                                    "id": 1,
                                                    "name": "Team A",
                                                    "generationId": 1
                                                  },
                                                  {
                                                    "id": 2,
                                                    "name": "Team B",
                                                    "generationId": 1
                                                  }
                                                ]
                                                """)))
            })
    public List<TeamResponse> getTeams(
            @Parameter(description = "기수 ID", example = "1") @RequestParam Long generationId) {
        return TeamResponse.fromList(teamService.findAllByGenerationId(generationId));
    }

    @GetMapping("/manager-roles")
    @Operation(
            summary = "매니저 업무 목록 조회",
            description =
                    "선택 가능한 매니저 업무 목록을 반환합니다.\n\n"
                            + "## 사용 시점\n"
                            + "매니저로 가입하는 경우 담당 업무를 선택합니다.\n\n"
                            + "## 매니저 역할 종류\n"
                            + "- TEAM_MANAGING: 팀 관리\n"
                            + "- ATTENDANCE_CHECK: 출석 체크\n"
                            + "- EVENT_PLANNING: 행사 기획\n"
                            + "- FACILITY_MANAGEMENT: 시설 관리\n"
                            + "- ETC: 기타\n\n"
                            + "## 주의사항\n"
                            + "- 매니저는 복수 선택 가능\n"
                            + "- 일반 멤버는 선택 불필요",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "매니저 역할 목록 조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        value =
                                                                """
                                                [
                                                  {
                                                    "code": "TEAM_MANAGING",
                                                    "name": "팀 관리"
                                                  },
                                                  {
                                                    "code": "ATTENDANCE_CHECK",
                                                    "name": "출석 체크"
                                                  }
                                                ]
                                                """)))
            })
    public List<ManagerRoleResponse> getManagerRoles() {
        return Arrays.stream(ManagerRole.values()).map(ManagerRoleResponse::from).toList();
    }
}
