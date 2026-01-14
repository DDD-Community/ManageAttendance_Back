package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserQrResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserRegisterRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.UserUpdateRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.UserWithdrawRequest;
import com.ddd.manage_attendance.domain.auth.domain.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 관리 API")
public class UserController {

    private final UserFacade userFacade;

    @PostMapping
    @Operation(
            summary = "[공개] 회원가입",
            description =
                    "신규 유저를 등록합니다.\n\n"
                            + "## 처리 흐름\n"
                            + "1. `/api/auth/login` 호출 → 202 응답 (신규 사용자)\n"
                            + "2. `/api/onboarding/verify-code` 로 초대 코드 검증\n"
                            + "3. `/api/onboarding/jobs` 직군 조회\n"
                            + "4. `/api/onboarding/teams` 팀 조회 (generationId 필요)\n"
                            + "5. `/api/onboarding/manager-roles` 매니저 역할 조회 (매니저인 경우)\n"
                            + "6. **본 API 호출** - 회원가입 완료\n\n"
                            + "## 필수 정보\n"
                            + "- **이름**: 사용자 이름\n"
                            + "- **기수 ID**: 초대 코드에서 확인한 기수\n"
                            + "- **직군**: BACKEND, FRONTEND, DESIGNER 등\n"
                            + "- **팀 ID**: 매니저가 아닌 경우 필수\n"
                            + "- **매니저 역할**: 매니저인 경우 필수\n"
                            + "- **OAuth 정보**: provider, token (로그인 시 받은 정보)\n"
                            + "- **초대 코드**: 검증된 초대 코드\n\n"
                            + "## 주의사항\n"
                            + "- 인증 불필요 (공개 API)\n"
                            + "- 초대 코드와 기수 ID가 일치해야 함\n"
                            + "- OAuth token으로 중복 가입 방지\n"
                            + "- 매니저는 teamId null 가능, 일반 멤버는 필수",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "회원가입 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = UserInfoResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        value =
                                                                """
                                                {
                                                  "id": 1,
                                                  "name": "홍길동",
                                                  "email": "hong@example.com",
                                                  "generationId": 1,
                                                  "generationName": "11기",
                                                  "jobRole": "BACKEND",
                                                  "teamId": 1,
                                                  "teamName": "Team A",
                                                  "managerRoles": [],
                                                  "isManager": false
                                                }
                                                """)))
            })
    public ResponseEntity<UserInfoResponse> registerUser(
            @Valid @RequestBody final UserRegisterRequest request) {
        UserInfoResponse response = userFacade.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/qr")
    @Operation(
            summary = "[공개] 유저 QR 조회",
            description =
                    "유저의 QR 코드를 조회합니다.\n\n"
                            + "## 사용 목적\n"
                            + "- 출석 체크용 QR 코드 생성\n"
                            + "- 사용자별 고유 QR 코드 제공\n\n"
                            + "## 주의사항\n"
                            + "- 인증 불필요 (공개 API)\n"
                            + "- 존재하지 않는 사용자 ID는 404 에러 발생")
    public UserQrResponse getUserQr(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable final Long id) {
        return userFacade.getUserQr(id);
    }

    @PutMapping("/me")
    @Operation(
            summary = "[인증] 내 정보 수정 (재등록)",
            description =
                    "온보딩 정보를 통해 내 정보를 수정(재등록) 합니다.\n\n"
                            + "## 사용 시나리오\n"
                            + "- 기수 변경\n"
                            + "- 팀 변경\n"
                            + "- 직군 변경\n"
                            + "- 매니저 역할 변경\n\n"
                            + "## 주의사항\n"
                            + "- JWT 토큰 필요 (Authorization: Bearer {token})\n"
                            + "- 초대 코드 재검증 필요\n"
                            + "- 출석 기록은 유지됨")
    @SecurityRequirement(name = "JWT")
    public UserInfoResponse updateUserInfo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody final UserUpdateRequest request) {
        return userFacade.updateUserInfo(userId, request);
    }

    @DeleteMapping("/me")
    @Operation(
            summary = "[인증] 회원 탈퇴",
            description =
                    "회원 탈퇴를 진행합니다.\n\n"
                            + "## 처리 내용\n"
                            + "- 사용자 정보 삭제\n"
                            + "- OAuth 연결 해제 (토큰 제공 시)\n"
                            + "- 출석 기록 삭제\n"
                            + "- Refresh Token 무효화\n\n"
                            + "## 주의사항\n"
                            + "- JWT 토큰 필요 (Authorization: Bearer {token})\n"
                            + "- OAuth 토큰 제공 시 OAuth 제공자와 연결 해제\n"
                            + "- **삭제된 데이터는 복구 불가**")
    @SecurityRequirement(name = "JWT")
    public void withdrawUser(
            @AuthenticationPrincipal Long userId,
            @RequestBody(required = false) UserWithdrawRequest request) {
        if (userId == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        String oauthToken = request != null ? request.token() : null;
        userFacade.withdrawUser(userId, oauthToken);
    }

    @PostMapping("/attendances/missing/generate")
    @Operation(
            summary = "[인증] 누락 출석 기록 생성",
            description = "누락된 유저의 출석 기록을 자동으로 생성합니다.\n\n" + "- JWT 토큰 필요")
    @SecurityRequirement(name = "JWT")
    public void generateMissingAttendances(@AuthenticationPrincipal Long userId) {
        userFacade.generateMissingAttendances(userId);
    }
}
