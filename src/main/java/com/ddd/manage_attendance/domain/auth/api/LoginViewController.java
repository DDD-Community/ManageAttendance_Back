package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.LoginResponse;
import com.ddd.manage_attendance.domain.auth.application.AuthService;
import com.ddd.manage_attendance.domain.auth.domain.OAuthProvider;
import com.ddd.manage_attendance.domain.oauth.infrastructure.apple.AppleAuthProperties;
import com.ddd.manage_attendance.domain.oauth.infrastructure.google.GoogleAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginViewController {

    private final GoogleAuthProperties googleAuthProperties;
    private final AppleAuthProperties appleAuthProperties;
    private final AuthService authService;

    @GetMapping("/auth/login")
    public String loginPage(Model model) {
        model.addAttribute("googleClientId", googleAuthProperties.getClientId());
        model.addAttribute("googleRedirectUri", googleAuthProperties.getRedirectUri());
        model.addAttribute("appleClientId", appleAuthProperties.getClientId());
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
            LoginResponse loginResponse = authService.login(OAuthProvider.GOOGLE, credential);
            model.addAttribute("loginResponse", loginResponse);
            return "oauth-test/success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "oauth-test/login";
        }
    }
}
