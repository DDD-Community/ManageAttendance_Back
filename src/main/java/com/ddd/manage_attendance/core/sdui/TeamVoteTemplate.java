package com.ddd.manage_attendance.core.sdui;

import java.util.List;

/**
 * 1단계 "팀 투표" 템플릿 정의(불변 스냅샷). Vote 엔티티에 text 컬럼으로 직렬화되어 저장되며, 버전 고정의 단위가 된다.
 *
 * <p>각 {@link Category} 는 안정 시맨틱 ID 를 가지며, 라벨/순서가 바뀌어도 ID 로 집계가 유지된다.
 */
public record TeamVoteTemplate(
        String title, String description, String notice, List<Category> categories) {

    /** 투표 부문(예: 기획 완성도 / 디자인 / 개발 완성도). */
    public record Category(
            String id,
            int order,
            String title,
            int maxSelectableTeams,
            boolean reasonRequired,
            int reasonMinLength,
            int reasonMaxLength,
            String reasonLabel) {}
}
