package com.ddd.manage_attendance.core.sdui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SDUI(Server-Driven UI) 컴포넌트 타입.
 *
 * <p>서버는 질문의 "의미 타입"만 내려주고, 실제 렌더링은 클라이언트가 소유한다. 새로운 타입 추가만 앱 배포가 필요하며 텍스트/옵션/순서/제약은 서버에서 변경한다.
 */
@Getter
@RequiredArgsConstructor
public enum VoteComponentType {
    TEAM_SELECT("팀 선택"),
    MULTI_SELECT("다중 선택"),
    LONG_TEXT("장문 텍스트"),
    BOOLEAN("예/아니오");

    private final String description;
}
