package com.ddd.manage_attendance.domain.vote.api.dto;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.ddd.manage_attendance.core.sdui.TeamVoteTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InternalVoteUpsertRequest(
        @NotBlank(message = "투표 제목은 필수입니다.") @Size(max = 100, message = "투표 제목은 100자 이하여야 합니다.")
                String title,
        TeamVoteTemplate teamVoteTemplate,
        FeedbackTemplate feedbackTemplate) {}
