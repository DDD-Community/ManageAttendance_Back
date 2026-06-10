package com.ddd.manage_attendance.domain.vote.api;

import com.ddd.manage_attendance.domain.vote.api.dto.ActiveVoteResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.FeedbackResultResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.FeedbackTemplateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.MyVoteStatusResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.TeamVoteResultResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.TeamVoteTemplateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteCreateRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteCreateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteDetailResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteNonRespondersResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteParticipationResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSummaryResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteTemplateUpdateRequest;
import com.ddd.manage_attendance.domain.vote.domain.VoteFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
@Tag(name = "Vote", description = "투표(팀 투표 / 참여 경험 피드백) API")
public class VoteController {

    private final VoteFacade voteFacade;

    @PostMapping
    @Operation(
            summary = "[운영진] 투표 생성",
            description = "팀 투표 / 피드백 템플릿을 가진 투표를 DRAFT 상태로 생성합니다.\n\n- 운영진 권한 필수")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<VoteCreateResponse> createVote(
            @AuthenticationPrincipal final Long userId,
            @RequestBody final VoteCreateRequest request) {
        final Long voteId = voteFacade.createVote(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(VoteCreateResponse.from(voteId));
    }

    @GetMapping
    @Operation(
            summary = "[운영진] 투표 목록 조회",
            description = "본인 기수의 전체 투표를 최신순으로 조회합니다.\n\n- 운영진 권한 필수")
    @SecurityRequirement(name = "JWT")
    public List<VoteSummaryResponse> getVotes(@AuthenticationPrincipal final Long userId) {
        return voteFacade.getVotesForManager(userId);
    }

    @GetMapping("/{voteId}")
    @Operation(
            summary = "[운영진] 투표 상세 조회",
            description = "투표 상태와 팀 투표/피드백 템플릿을 함께 조회합니다(편집 재개용).\n\n- 운영진 권한 필수")
    @SecurityRequirement(name = "JWT")
    public VoteDetailResponse getVoteDetail(
            @AuthenticationPrincipal final Long userId, @PathVariable final Long voteId) {
        return voteFacade.getVoteDetail(userId, voteId);
    }

    @PutMapping("/{voteId}/template")
    @Operation(
            summary = "[운영진] 투표 템플릿 수정",
            description = "DRAFT 상태에서만 템플릿을 수정합니다. 수정 시 템플릿 버전이 증가합니다.\n\n- 운영진 권한 필수")
    @SecurityRequirement(name = "JWT")
    public void updateTemplate(
            @AuthenticationPrincipal final Long userId,
            @PathVariable final Long voteId,
            @RequestBody final VoteTemplateUpdateRequest request) {
        voteFacade.updateTemplate(userId, voteId, request);
    }

    @PatchMapping("/{voteId}/open")
    @Operation(
            summary = "[운영진] 투표 시작",
            description = "투표를 OPEN 상태로 전환합니다(템플릿 freeze).\n\n- 운영진 권한 필수\n- DRAFT 상태에서만 가능")
    @SecurityRequirement(name = "JWT")
    public void openVote(
            @AuthenticationPrincipal final Long userId, @PathVariable final Long voteId) {
        voteFacade.openVote(userId, voteId);
    }

    @PatchMapping("/{voteId}/close")
    @Operation(
            summary = "[운영진] 투표 종료",
            description = "투표를 CLOSED 상태로 전환합니다(불가역).\n\n- 운영진 권한 필수\n- OPEN 상태에서만 가능")
    @SecurityRequirement(name = "JWT")
    public void closeVote(
            @AuthenticationPrincipal final Long userId, @PathVariable final Long voteId) {
        voteFacade.closeVote(userId, voteId);
    }

    @GetMapping("/{voteId}/team-vote/template")
    @Operation(
            summary = "[멤버] 팀 투표 템플릿 조회",
            description = "1단계 팀 투표 화면을 그릴 템플릿과 팀 목록을 조회합니다.\n\n- 본인 팀은 isOwnTeam=true 로 표시")
    @SecurityRequirement(name = "JWT")
    public TeamVoteTemplateResponse getTeamVoteTemplate(
            @AuthenticationPrincipal final Long userId, @PathVariable final Long voteId) {
        return voteFacade.getTeamVoteTemplate(userId, voteId);
    }

    @GetMapping("/{voteId}/feedback/template")
    @Operation(summary = "[멤버] 참여 경험 피드백 템플릿 조회", description = "2단계 피드백 화면을 그릴 템플릿을 조회합니다.")
    @SecurityRequirement(name = "JWT")
    public FeedbackTemplateResponse getFeedbackTemplate(
            @AuthenticationPrincipal final Long userId, @PathVariable final Long voteId) {
        return voteFacade.getFeedbackTemplate(userId, voteId);
    }

    @GetMapping("/{voteId}/team-vote/results")
    @Operation(
            summary = "[운영진] 팀 투표 결과 집계 조회",
            description =
                    "부문별 팀 득표 순위와 작성된 사유 목록을 조회합니다.\n\n"
                            + "- 운영진 권한 필수\n"
                            + "- OPEN/CLOSED 모두 실시간 집계(DRAFT 는 빈 결과)")
    @SecurityRequirement(name = "JWT")
    public TeamVoteResultResponse getTeamVoteResults(
            @AuthenticationPrincipal final Long userId, @PathVariable final Long voteId) {
        return voteFacade.getTeamVoteResults(userId, voteId);
    }

    @GetMapping("/{voteId}/feedback/results")
    @Operation(
            summary = "[운영진] 참여 경험 피드백 결과 집계 조회",
            description =
                    "질문별 응답 분포(선택지/예·아니오)와 작성 텍스트(익명)를 조회합니다.\n\n"
                            + "- 운영진 권한 필수\n"
                            + "- followUp 후속 질문도 함께 집계")
    @SecurityRequirement(name = "JWT")
    public FeedbackResultResponse getFeedbackResults(
            @AuthenticationPrincipal final Long userId, @PathVariable final Long voteId) {
        return voteFacade.getFeedbackResults(userId, voteId);
    }

    @PostMapping("/{voteId}/responses")
    @Operation(
            summary = "[멤버] 투표 제출",
            description =
                    "팀 투표 + 피드백을 한 번에 제출합니다.\n\n"
                            + "- OPEN 상태에서만 가능\n"
                            + "- 1인 1응답(재투표 불가)\n"
                            + "- 서버가 제약(선택 개수/본인 팀 제외/글자수/필수)을 재검증")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<Void> submit(
            @AuthenticationPrincipal final Long userId,
            @PathVariable final Long voteId,
            @RequestBody final VoteSubmitRequest request) {
        voteFacade.submit(userId, voteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/active")
    @Operation(
            summary = "[멤버] 진행 중 투표 조회",
            description =
                    "내 기수에 진행 중(OPEN)인 투표가 있는지 조회합니다.\n\n"
                            + "- 홈 메뉴의 '투표(NEW)' 노출/진입 판단에 사용\n"
                            + "- 없으면 hasActiveVote=false\n"
                            + "- alreadyResponded 로 완료 화면 분기")
    @SecurityRequirement(name = "JWT")
    public ActiveVoteResponse getActiveVote(@AuthenticationPrincipal final Long userId) {
        return voteFacade.getActiveVote(userId);
    }

    @GetMapping("/{voteId}/responses/me")
    @Operation(
            summary = "[멤버] 내 참여 여부 조회",
            description = "특정 투표에 내가 이미 참여했는지 조회합니다.\n\n- 완료/재참여 차단 화면 판단에 사용")
    @SecurityRequirement(name = "JWT")
    public MyVoteStatusResponse getMyVoteStatus(
            @AuthenticationPrincipal final Long userId, @PathVariable final Long voteId) {
        return voteFacade.getMyVoteStatus(userId, voteId);
    }

    @GetMapping("/{voteId}/participation")
    @Operation(
            summary = "[운영진] 투표 상태 및 참여 현황 조회",
            description =
                    "투표 관리 화면 데이터를 조회합니다.\n\n"
                            + "- 운영진 권한 필수\n"
                            + "- 투표 상태(DRAFT/OPEN/CLOSED) + 대상 멤버수/참여수/참여율(운영진 제외)")
    @SecurityRequirement(name = "JWT")
    public VoteParticipationResponse getParticipation(
            @AuthenticationPrincipal final Long userId, @PathVariable final Long voteId) {
        return voteFacade.getParticipation(userId, voteId);
    }

    @GetMapping("/{voteId}/non-responders")
    @Operation(
            summary = "[운영진] 미참여 멤버 명단 조회",
            description = "아직 투표하지 않은 멤버 명단(이름 + 소속 팀명)을 조회합니다.\n\n- 운영진 권한 필수")
    @SecurityRequirement(name = "JWT")
    public VoteNonRespondersResponse getNonResponders(
            @AuthenticationPrincipal final Long userId, @PathVariable final Long voteId) {
        return voteFacade.getNonResponders(userId, voteId);
    }
}
