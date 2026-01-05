package com.ddd.manage_attendance.domain.oauth.domain.dto;

public record OAuthRevocationRequest(String tokenFromClient, String refreshTokenFromDb) {}
