package com.ddd.manage_attendance.domain.sample.api.dto;

import com.ddd.manage_attendance.domain.sample.domain.Sample;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[샘플] 샘플 단건 조회 응답 DTO")
public record SampleResponse(
        @Schema(description = "샘플 고유 ID", example = "1") Long id,
        @Schema(description = "제목", example = "제목입니다.") String title,
        @Schema(description = "내용", example = "내용입니다.") String content) {

    public static SampleResponse from(final Sample sample) {
        return new SampleResponse(sample.getId(), sample.getTitle(), sample.getContent());
    }
}
