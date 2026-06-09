package com.ddd.manage_attendance.core.sdui;

import java.util.List;

/**
 * 2단계 "참여 경험 피드백" 템플릿 정의(불변 스냅샷).
 *
 * <p>{@link Question} 은 {@link VoteComponentType} 에 따라 해석된다.
 *
 * <ul>
 *   <li>MULTI_SELECT — {@code options} 중 다중 선택, {@code maxSelectableOptions}(null=무제한), 선택적 {@code
 *       followUp} 후속 질문
 *   <li>LONG_TEXT — {@code maxLength} 제한 텍스트
 *   <li>BOOLEAN — 예/아니오
 * </ul>
 */
public record FeedbackTemplate(String title, String description, List<Question> questions) {

    public record Question(
            String id,
            int order,
            VoteComponentType type,
            String title,
            String helpText,
            boolean required,
            Integer maxSelectableOptions,
            Integer maxLength,
            List<Option> options,
            Question followUp) {}

    public record Option(String id, String label) {}
}
