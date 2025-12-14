package com.ddd.manage_attendance.domain.oauth.domain.dto;

import com.ddd.manage_attendance.domain.auth.domain.User;

public record OAuthLoginResult(User user, boolean isNewUser) {}
