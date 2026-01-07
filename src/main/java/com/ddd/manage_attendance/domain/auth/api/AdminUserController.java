package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.auth.domain.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "[운영진] 회원 관리 API", description = "운영진 회원 관리 API 입니다.")
public class AdminUserController {

    private final UserFacade userFacade;

    @GetMapping
    @Operation(summary = "전체 회원 조회", description = "전체 회원을 조회합니다. (이름 검색 가능)")
    public List<UserInfoResponse> searchUsers(@RequestParam(required = false) String name) {
        return userFacade.searchUsers(name);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "회원 강제 삭제", description = "회원을 강제로 삭제합니다. (복구 불가)")
    public void forceDeleteUser(@PathVariable Long userId) {
        userFacade.forceDeleteUser(userId);
    }
}
