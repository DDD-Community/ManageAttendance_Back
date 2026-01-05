package com.ddd.manage_attendance.ui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestViewController {

    @Value("${google.auth.client-id}")
    private String googleClientId;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("googleClientId", googleClientId);
        return "test/login";
    }

    @GetMapping("/register")
    public String register() {
        return "test/register";
    }

    @GetMapping("/my")
    public String myPage() {
        return "test/my";
    }
}
