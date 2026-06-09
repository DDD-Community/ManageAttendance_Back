package com.ddd.manage_attendance.domain.vote.api;

import com.ddd.manage_attendance.domain.vote.api.dto.FeedbackTemplateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.TeamVoteTemplateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteCreateRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteCreateResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteSubmitRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteTemplateUpdateRequest;
import com.ddd.manage_attendance.domain.vote.domain.VoteFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}
