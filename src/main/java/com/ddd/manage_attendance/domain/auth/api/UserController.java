package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.UserQrResponse;
import com.ddd.manage_attendance.domain.auth.domain.UserQrFacade;
import com.ddd.manage_attendance.domain.auth.domain.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserQrFacade userQrFacade;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "유저 저장", description = "유저를 저장 합니다.")
    public void registerUser(final String name) {
        userService.registerUser(name);
    }

    @GetMapping("/{id}/qr")
    @Operation(summary = "유저 QR 조회", description = "유저 QR를 조회 합니다.")
    public UserQrResponse getUserQr(@PathVariable final Long id) {
        return userQrFacade.getUserQr(id);
    }
}
