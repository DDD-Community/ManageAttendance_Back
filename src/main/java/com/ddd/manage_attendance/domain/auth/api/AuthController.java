package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.LoginRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.LoginResponse;
import com.ddd.manage_attendance.domain.auth.domain.AuthFacade;
import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 API")
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/login")
    @Operation(summary = "OAuth 로그인", description = "OAuth 제공자를 통해 로그인합니다.")
    public LoginResponse login(@Valid @RequestBody final LoginRequest request) {
        final OAuthProvider provider = request.provider();
        final String token = request.token();
        return authFacade.login(provider, token, null);
    }
}
