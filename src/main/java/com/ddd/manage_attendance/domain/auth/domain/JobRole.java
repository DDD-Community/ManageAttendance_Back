package com.ddd.manage_attendance.domain.auth.domain;

import lombok.Getter;

@Getter
public enum JobRole {
    BACKEND("BE"),
    FRONTEND("FE"),
    DESIGNER("PM"),
    PM("PD"),
    ANDROID("AND"),
    IOS("IOS");

    private final String description;

    JobRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
