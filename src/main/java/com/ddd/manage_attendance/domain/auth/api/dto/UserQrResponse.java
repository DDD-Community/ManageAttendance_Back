package com.ddd.manage_attendance.domain.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "[유저] QR 코드 응답 DTO")
public record UserQrResponse(
        @Schema(description = "유저 ID", example = "1") Long id,
        @Schema(description = "QR 코드 (Base64 인코딩)", example = "iVBORw0KGgoAAAANSUhEUgAA...")
                String qrBase64) {
    public static UserQrResponse from(final Long userId, final String qrCode) {
        return new UserQrResponse(userId, qrCode);
    }
}
