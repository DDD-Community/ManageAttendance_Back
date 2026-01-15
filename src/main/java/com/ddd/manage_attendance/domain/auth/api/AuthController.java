package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.LoginRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.LoginResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.RefreshTokenRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.RefreshTokenResponse;
import com.ddd.manage_attendance.domain.auth.domain.AuthFacade;
import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 API (로그인, 로그아웃, 토큰 갱신)")
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/login")
    @Operation(
            summary = "[공개] OAuth 로그인",
            description =
                    "OAuth 제공자(Google/Apple)를 통해 로그인합니다.\n\n"
                            + "## 응답 상태 코드\n"
                            + "- **201**: 기존 회원 로그인 성공 - JWT 토큰 발급\n"
                            + "- **202**: 신규 사용자 (회원가입 필요) - isNewUser=true 반환\n\n"
                            + "## 처리 흐름\n"
                            + "1. OAuth 토큰 검증 (Google/Apple)\n"
                            + "2. OAuth ID(sub)로 기존 회원 조회\n"
                            + "3. 기존 회원: Access/Refresh Token 발급 (201)\n"
                            + "4. 신규 사용자: 회원가입 안내 (202)\n\n"
                            + "## 주의사항\n"
                            + "- 인증 불필요 (공개 API)\n"
                            + "- Google: id_token 사용 권장\n"
                            + "- Apple: identity token 사용\n"
                            + "- 신규 사용자는 `/api/users` POST로 회원가입 필요",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "로그인 성공 (기존 회원)",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = LoginResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        value =
                                                                """
                                                {
                                                  "userId": 1,
                                                  "name": "홍길동",
                                                  "email": "hong@example.com",
                                                  "oauthProvider": "GOOGLE",
                                                  "message": "로그인 성공",
                                                  "isNewUser": false,
                                                  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                  "oauthRefreshToken": null,
                                                  "role": "MEMBER"
                                                }
                                                """))),
                @ApiResponse(
                        responseCode = "202",
                        description = "회원가입 필요 (신규 사용자)",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = LoginResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        value =
                                                                """
                                                {
                                                  "userId": null,
                                                  "name": null,
                                                  "email": "newuser@example.com",
                                                  "oauthProvider": "GOOGLE",
                                                  "message": "회원가입이 필요합니다",
                                                  "isNewUser": true,
                                                  "accessToken": null,
                                                  "refreshToken": null,
                                                  "oauthRefreshToken": "1//0gWxxx...",
                                                  "role": null
                                                }
                                                """)))
            })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
        final OAuthProvider provider = request.provider();
        final String token = request.token();
        final LoginResponse response = authFacade.login(provider, token, null);

        // 신규 사용자(회원가입 필요): 202, 기존 회원(로그인 성공): 201
        final HttpStatus status = response.isNewUser() ? HttpStatus.ACCEPTED : HttpStatus.CREATED;

        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "[공개] 토큰 재발급",
            description =
                    "Refresh Token으로 새로운 Access Token과 Refresh Token을 발급받습니다.\n\n"
                            + "## 처리 흐름\n"
                            + "1. Refresh Token 검증\n"
                            + "2. 새로운 Access Token 발급\n"
                            + "3. 새로운 Refresh Token 발급 (보안 강화)\n\n"
                            + "## 주의사항\n"
                            + "- 인증 불필요 (Refresh Token으로 검증)\n"
                            + "- 만료되거나 유효하지 않은 Refresh Token은 401 에러 발생\n"
                            + "- 기존 Refresh Token은 무효화됨",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "토큰 재발급 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = RefreshTokenResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        value =
                                                                """
                                                {
                                                  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                                }
                                                """)))
            })
    public RefreshTokenResponse refresh(@Valid @RequestBody final RefreshTokenRequest request) {
        return authFacade.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(
            summary = "[인증] 로그아웃",
            description =
                    "로그아웃하고 Refresh Token을 무효화합니다.\n\n"
                            + "## 처리 흐름\n"
                            + "1. JWT 토큰에서 사용자 식별\n"
                            + "2. 해당 사용자의 Refresh Token 무효화\n\n"
                            + "## 주의사항\n"
                            + "- JWT Access Token 필요 (Authorization: Bearer {token})\n"
                            + "- Refresh Token은 무효화되어 재사용 불가",
            responses = {@ApiResponse(responseCode = "200", description = "로그아웃 성공")})
    @SecurityRequirement(name = "JWT")
    public void logout(@AuthenticationPrincipal Long userId) {
        authFacade.logout(userId);
    }
}
