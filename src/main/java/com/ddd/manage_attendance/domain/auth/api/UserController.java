package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserQrResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserRegisterRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.UserUpdateRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.UserWithdrawRequest;
import com.ddd.manage_attendance.domain.auth.domain.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
                            + "- 인증 불필요 (회원가입 API)\n"
                            + "- 초대 코드 검증 필수\n"
                            + "- OAuth 인증 후 호출")
    public UserInfoResponse registerUser(@Valid @RequestBody final UserRegisterRequest request) {
        return userFacade.registerUser(request);
    }

    @GetMapping("/{id}/qr")
    @Operation(summary = "[공개] 유저 QR 조회", description = "유저의 QR 코드를 조회합니다.\n\n" + "- 인증 불필요")
    public UserQrResponse getUserQr(@PathVariable final Long id) {
        return userFacade.getUserQr(id);
    }

    @PutMapping("/me")
    @Operation(
            summary = "[인증] 내 정보 수정 (재등록)",
            description = "온보딩 정보를 통해 내 정보를 수정(재등록) 합니다.\n\n" + "- JWT 토큰 필요")
    @SecurityRequirement(name = "JWT")
    public UserInfoResponse updateUserInfo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody final UserUpdateRequest request) {
        return userFacade.updateUserInfo(userId, request);
    }

    @DeleteMapping("/me")
    @Operation(
            summary = "[인증] 회원 탈퇴",
            description = "회원 탈퇴를 진행합니다. (OAuth 토큰이 있으면 연결 해제 포함)\n\n" + "- JWT 토큰 필요")
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
    @Operation(summary = "누락 된 유저 출석 기록 저장", description = "누락 된 유저 출석 기록을 저장 합니다.")
    public void generateMissingAttendances(@AuthenticationPrincipal Long userId) {
        userFacade.generateMissingAttendances(userId);
    }
}
