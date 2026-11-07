package com.ddd.manage_attendance.domain.vote.api;

import com.ddd.manage_attendance.core.util.TimeProvider;
import com.ddd.manage_attendance.domain.vote.api.dto.FeedbackResultResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.InternalVoteDetail;
import com.ddd.manage_attendance.domain.vote.api.dto.InternalVoteResponseExport;
import com.ddd.manage_attendance.domain.vote.api.dto.InternalVoteSummary;
import com.ddd.manage_attendance.domain.vote.api.dto.InternalVoteUpsertRequest;
import com.ddd.manage_attendance.domain.vote.api.dto.TeamVoteResultResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteNonRespondersResponse;
import com.ddd.manage_attendance.domain.vote.api.dto.VoteParticipationResponse;
import com.ddd.manage_attendance.domain.vote.domain.InternalVoteFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Tag(name = "내부 투표 API", description = "내부 시스템에서 투표를 관리하고 응답을 조회합니다.")
@SecurityRequirement(name = "InternalApiKey")
public class InternalVoteController {
    private final InternalVoteFacade facade;
    private final TimeProvider timeProvider;

    @GetMapping("/generations/{generationId}/votes")
    @Operation(summary = "[내부] 기수별 투표 목록 조회")
    public List<InternalVoteSummary> list(@PathVariable @Positive final Long generationId) {
        return facade.list(generationId);
    }

    @PostMapping("/generations/{generationId}/votes")
    @Operation(summary = "[내부] 투표 생성")
    public ResponseEntity<InternalVoteDetail> create(
            @PathVariable @Positive final Long generationId,
            @Valid @RequestBody final InternalVoteUpsertRequest request) {
        final InternalVoteDetail result = facade.create(generationId, request);
        return ResponseEntity.created(URI.create("/api/internal/votes/" + result.voteId()))
                .body(result);
    }

    @GetMapping("/votes/{voteId}")
    @Operation(summary = "[내부] 투표 상세 조회")
    public InternalVoteDetail get(@PathVariable @Positive final Long voteId) {
        return facade.get(voteId);
    }

    @PutMapping("/votes/{voteId}")
    @Operation(summary = "[내부] DRAFT 투표 수정")
    public InternalVoteDetail update(
            @PathVariable @Positive final Long voteId,
            @Valid @RequestBody final InternalVoteUpsertRequest request) {
        return facade.update(voteId, request);
    }

    @PutMapping("/votes/{voteId}/text")
    @Operation(summary = "[내부] OPEN 투표 글자 수정")
    public InternalVoteDetail reviseText(
            @PathVariable @Positive final Long voteId,
            @Valid @RequestBody final InternalVoteUpsertRequest request) {
        return facade.reviseText(voteId, request);
    }

    @DeleteMapping("/votes/{voteId}")
    @Operation(summary = "[내부] DRAFT 투표 삭제")
    public ResponseEntity<Void> delete(@PathVariable @Positive final Long voteId) {
        facade.delete(voteId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/votes/{voteId}/open")
    @Operation(summary = "[내부] 투표 시작")
    public InternalVoteDetail open(@PathVariable @Positive final Long voteId) {
        return facade.open(voteId, timeProvider.nowDateTime());
    }

    @PatchMapping("/votes/{voteId}/close")
    @Operation(summary = "[내부] 투표 종료")
    public InternalVoteDetail close(@PathVariable @Positive final Long voteId) {
        return facade.close(voteId, timeProvider.nowDateTime());
    }

    @GetMapping("/votes/{voteId}/participation")
    @Operation(summary = "[내부] 투표 참여 현황 조회")
    public VoteParticipationResponse participation(@PathVariable @Positive final Long voteId) {
        return facade.participation(voteId);
    }

    @GetMapping("/votes/{voteId}/non-responders")
    @Operation(summary = "[내부] 미응답자 조회")
    public VoteNonRespondersResponse nonResponders(@PathVariable @Positive final Long voteId) {
        return facade.nonResponders(voteId);
    }

    @GetMapping("/votes/{voteId}/team-vote/results")
    @Operation(summary = "[내부] 팀 투표 결과 조회")
    public TeamVoteResultResponse teamResults(@PathVariable @Positive final Long voteId) {
        return facade.teamResults(voteId);
    }

    @GetMapping("/votes/{voteId}/feedback/results")
    @Operation(summary = "[내부] 피드백 결과 조회")
    public FeedbackResultResponse feedbackResults(@PathVariable @Positive final Long voteId) {
        return facade.feedbackResults(voteId);
    }

    @GetMapping("/votes/{voteId}/responses")
    @Operation(summary = "[내부] 실명 투표 응답 원본 조회")
    public InternalVoteResponseExport responses(@PathVariable @Positive final Long voteId) {
        return facade.responses(voteId);
    }
}
