package com.ddd.manage_attendance.domain.auth.api;

import com.ddd.manage_attendance.domain.attendance.api.dto.AttendanceSummaryResponse;
import com.ddd.manage_attendance.domain.attendance.api.dto.TeamAttendancesResponse;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceFacade;
import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.auth.domain.MyPageFacade;
import com.ddd.manage_attendance.domain.team.api.dto.TeamResponse;
import com.ddd.manage_attendance.domain.team.domain.TeamFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Admin MyPage", description = "운영진 전용 마이페이지 API - 모든 API 운영진 권한 필요")
@PreAuthorize("hasAuthority('MANAGER')")
@SecurityRequirement(name = "JWT")
public class AdminMyPageController {
    private final AttendanceFacade attendanceFacade;
    private final TeamFacade teamFacade;
    private final MyPageFacade myPageFacade;

    @GetMapping
    @Operation(summary = "[운영진] 내 정보 조회", description = "운영진의 정보를 조회합니다.")
    public UserInfoResponse getMyInfo(@AuthenticationPrincipal Long userId) {
        return myPageFacade.getMyInfo(userId);
    }

    @GetMapping("/schedules/{scheduleId}/attendances")
    @Operation(summary = "[운영진] 기수 출석 현황 요약 조회", description = "기수 출석 현황 요약을 조회합니다.")
    public AttendanceSummaryResponse getGenerationAttendanceSummaryByScheduleId(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "스케줄 ID", example = "1") @PathVariable @Positive
                    final Long scheduleId) {
        return attendanceFacade.getGenerationAttendanceSummaryByScheduleId(userId, scheduleId);
    }

    @GetMapping("/generations/teams")
    @Operation(summary = "[운영진] 현재 기수 팀 이름 조회", description = "현재 기수의 팀 이름을 조회합니다.")
    public List<TeamResponse> getCurrentGenerationTeams(
            @AuthenticationPrincipal final Long userId) {
        return teamFacade.getCurrentGenerationTeams(userId);
    }

    @GetMapping("/schedules/{scheduleId}/teams/{teamId}/attendances")
    @Operation(summary = "[운영진] 세션별 팀 멤버 출석 현황 조회", description = "특정 세션의 팀 멤버 출석 현황을 조회합니다.")
    public List<TeamAttendancesResponse> getTeamAttendancesByScheduleId(
            @AuthenticationPrincipal final Long userId,
            @Parameter(description = "스케줄 ID", example = "1") @PathVariable @Positive
                    final Long scheduleId,
            @Parameter(description = "팀 ID", example = "1") @PathVariable @Positive
                    final Long teamId) {
        return attendanceFacade.getTeamAttendancesByScheduleId(userId, scheduleId, teamId);
    }
}
