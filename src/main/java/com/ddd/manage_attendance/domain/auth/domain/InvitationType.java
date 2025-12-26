package com.ddd.manage_attendance.domain.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvitationType {
    MEMBER("일반 멤버"),
    MANAGER("운영진");

    private final String description;
}
