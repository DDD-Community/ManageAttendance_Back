package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceSummaryResponse;
import com.ddd.manage_attendance.domain.attendance.api.dto.TeamAttendancesResponse;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceFacade;
import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.auth.domain.MyPageFacade;
import com.ddd.manage_attendance.domain.team.api.dto.TeamResponse;
import com.ddd.manage_attendance.domain.team.domain.TeamFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/me")
@RequiredArgsConstructor
@Tag(name = "[운영진] 마이페이지 API", description = "운영진 마이페이지 API 입니다.")
@PreAuthorize("hasAuthority('MANAGER')")
public class AdminMyPageController {
    private final AttendanceFacade attendanceFacade;
    private final TeamFacade teamFacade;
    private final MyPageFacade myPageFacade;

    @GetMapping
    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 정보를 조회합니다.")
    public UserInfoResponse getMyInfo(@AuthenticationPrincipal Long userId) {
        return myPageFacade.getMyInfo(userId);
    }

    @GetMapping("/schedules/{scheduleId}/attendances")
    @Operation(summary = "기수 출석 현황 요약 조회", description = "기수 출석 현황 요약을 조회 합니다.")
    public AttendanceSummaryResponse getGenerationAttendanceSummaryByScheduleId(
            @AuthenticationPrincipal Long userId, @PathVariable @Positive final Long scheduleId) {
        return attendanceFacade.getGenerationAttendanceSummaryByScheduleId(userId, scheduleId);
    }

    @GetMapping("/generations/teams")
    @Operation(summary = "현재 기수 팀 이름 조회", description = "현재 기수 팀 이름을 조회 합니다.")
    public List<TeamResponse> getCurrentGenerationTeams(
            @AuthenticationPrincipal final Long userId) {
        return teamFacade.getCurrentGenerationTeams(userId);
    }

    @GetMapping("/schedules/{scheduleId}/teams/{teamId}/attendances")
    @Operation(summary = "세션 별 팀 멤버 출석 현황 조회", description = "세션 별 팀 멤버 출석 현황을 조회 합니다.")
    public List<TeamAttendancesResponse> getTeamAttendancesByScheduleId(
            @AuthenticationPrincipal final Long userId,
            @PathVariable @Positive final Long scheduleId,
            @PathVariable @Positive final Long teamId) {
        return attendanceFacade.getTeamAttendancesByScheduleId(userId, scheduleId, teamId);
    }
}
