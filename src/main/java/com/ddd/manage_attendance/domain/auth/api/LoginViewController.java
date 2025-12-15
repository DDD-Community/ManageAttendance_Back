package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.LoginResponse;
import com.ddd.manage_attendance.domain.auth.domain.AuthFacade;
import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import com.ddd.manage_attendance.domain.oauth.infrastructure.apple.AppleAuthProperties;
import com.ddd.manage_attendance.domain.oauth.infrastructure.apple.AppleUserInfoParser;
import com.ddd.manage_attendance.domain.oauth.infrastructure.google.GoogleAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginViewController {

    private final GoogleAuthProperties googleAuthProperties;
    private final AppleAuthProperties appleAuthProperties;
    private final AuthFacade authFacade;
    private final AppleUserInfoParser appleUserInfoParser;

    @GetMapping("/auth/login")
    public String loginPage(Model model) {
        model.addAttribute("googleClientId", googleAuthProperties.getClientId());
        model.addAttribute("googleRedirectUri", googleAuthProperties.getRedirectUri());
        model.addAttribute("appleServiceId", appleAuthProperties.getServiceId());
        model.addAttribute("appleRedirectUri", appleAuthProperties.getRedirectUri());
        return "oauth-test/login";
    }

    @GetMapping("/google/callback")
    public String googleCallback(
            @RequestParam(value = "credential", required = false) String credential, Model model) {
        if (credential == null || credential.trim().isEmpty()) {
            model.addAttribute("error", "인증 정보를 받을 수 없습니다.");
            return "oauth-test/login";
        }

        try {
            LoginResponse loginResponse = authFacade.login(OAuthProvider.GOOGLE, credential, null);
            model.addAttribute("loginResponse", loginResponse);
            return "oauth-test/success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "oauth-test/login";
        }
    }

    @PostMapping(
            value = "/apple/callback",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> appleCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "id_token", required = false) String idToken,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "state", required = false) String state) {
        if (idToken == null || idToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("인증 정보를 받을 수 없습니다."));
        }

        try {
            final String userName = appleUserInfoParser.extractUserName(user);
            LoginResponse loginResponse = authFacade.login(OAuthProvider.APPLE, idToken, userName);
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("로그인 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /** 에러 응답용 내부 클래스 */
    private record ErrorResponse(String message) {}
}
