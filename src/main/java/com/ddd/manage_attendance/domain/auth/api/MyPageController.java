package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceSummaryResponse;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceFacade;
import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.auth.domain.MyPageFacade;
import com.ddd.manage_attendance.domain.schedule.api.dto.ScheduleWithAttendanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Tag(name = "마이페이지 API", description = "마이페이지 API 입니다.")
public class MyPageController {
    private final AttendanceFacade attendanceFacade;
    private final MyPageFacade myPageFacade;

    @GetMapping
    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 정보를 조회합니다.")
    public UserInfoResponse getMyInfo(@AuthenticationPrincipal Long userId) {
        return myPageFacade.getMyInfo(userId);
    }

    @GetMapping("/attendances")
    @Operation(summary = "내 출석 현황 요약 조회", description = "내 출석 현황 요약을 조회 합니다.")
    public AttendanceSummaryResponse getMyAttendanceSummary(
            @AuthenticationPrincipal final Long userId) {
        return attendanceFacade.getMyAttendanceSummary(userId);
    }

    @GetMapping("/schedules")
    @Operation(summary = "내 스케줄/출석 현황 조회", description = "전체 스케줄/출석 현황을 조회 합니다.")
    public List<ScheduleWithAttendanceResponse> getAllMySchedules(
            @AuthenticationPrincipal Long userId) {
        return attendanceFacade.getAllMySchedules(userId);
    }
}
