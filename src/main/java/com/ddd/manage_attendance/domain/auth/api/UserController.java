package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserQrResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserRegisterRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.UserUpdateRequest;
import com.ddd.manage_attendance.domain.auth.domain.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Tag(name = "유저 API", description = "유저 API 입니다.")
public class UserController {

    private final UserFacade userFacade;

    @PostMapping
    @Operation(summary = "유저 저장", description = "유저를 저장 합니다.")
    public UserInfoResponse registerUser(@Valid @RequestBody final UserRegisterRequest request) {
        return userFacade.registerUser(request);
    }

    @GetMapping("/{id}/qr")
    @Operation(summary = "유저 QR 조회", description = "유저 QR를 조회 합니다.")
    public UserQrResponse getUserQr(@PathVariable final Long id) {
        return userFacade.getUserQr(id);
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 정보를 조회합니다.")
    public UserInfoResponse getMyInfo(@AuthenticationPrincipal Long userId) {
        return userFacade.getUserInfo(userId);
    }

    @PutMapping("/me")
    @Operation(summary = "내 정보 수정 (재등록)", description = "온보딩 정보를 통해 내 정보를 수정(재등록) 합니다.")
    public UserInfoResponse updateUserInfo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody final UserUpdateRequest request) {
        return userFacade.updateUserInfo(userId, request);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다. (OAuth 토큰이 있으면 연결 해제 포함)")
    public void withdrawUser(
            @AuthenticationPrincipal Long userId,
            @RequestBody(required = false)
                    com.ddd.manage_attendance.domain.auth.api.dto.UserWithdrawRequest request) {
        String oauthToken = request != null ? request.token() : null;
        userFacade.withdrawUser(userId, oauthToken);
    }
}
