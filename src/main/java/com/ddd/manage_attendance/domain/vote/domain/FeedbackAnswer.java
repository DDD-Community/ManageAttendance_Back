package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/**
 * 피드백 응답 1건. 질문 타입에 따라 채워지는 값이 다르다.
 *
 * <ul>
 *   <li>MULTI_SELECT — {@code optionId} (선택지마다 1행)
 *   <li>LONG_TEXT — {@code textValue}
 *   <li>BOOLEAN — {@code boolValue}
 * </ul>
 *
 * {@code questionId} 는 템플릿의 안정 시맨틱 ID 이므로 라벨이 바뀌어도 집계가 유지된다.
 */
@Getter
@Entity
@Table(name = "feedback_answer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @NotNull
    @Comment("응답 Id")
    @Column(name = "response_id", nullable = false, columnDefinition = "bigint")
    private Long responseId;

    @NotNull
    @Comment("질문 Id (템플릿의 안정 시맨틱 Id)")
    @Column(name = "question_id", nullable = false, columnDefinition = "varchar(100)")
    private String questionId;

    @Comment("선택지 ID (MULTI_SELECT)")
    @Column(name = "option_id", nullable = true, columnDefinition = "varchar(100)")
    private String optionId;

    @Comment("텍스트 응답 (LONG_TEXT)")
    @Column(name = "text_value", nullable = true, columnDefinition = "varchar(500)")
    private String textValue;

    @Comment("예/아니오 응답 (BOOLEAN)")
    @Column(name = "bool_value", nullable = true, columnDefinition = "bit(1)")
    private Boolean boolValue;

    @Builder(access = AccessLevel.PRIVATE)
    public FeedbackAnswer(
            Long responseId,
            String questionId,
            String optionId,
            String textValue,
            Boolean boolValue) {
        this.responseId = responseId;
        this.questionId = questionId;
        this.optionId = optionId;
        this.textValue = textValue;
        this.boolValue = boolValue;
    }

    public static FeedbackAnswer ofOption(
            final Long responseId, final String questionId, final String optionId) {
        return FeedbackAnswer.builder()
                .responseId(responseId)
                .questionId(questionId)
                .optionId(optionId)
                .build();
    }

    public static FeedbackAnswer ofText(
            final Long responseId, final String questionId, final String textValue) {
        return FeedbackAnswer.builder()
                .responseId(responseId)
                .questionId(questionId)
                .textValue(textValue)
                .build();
    }

    public static FeedbackAnswer ofBool(
            final Long responseId, final String questionId, final Boolean boolValue) {
        return FeedbackAnswer.builder()
                .responseId(responseId)
                .questionId(questionId)
                .boolValue(boolValue)
                .build();
    }
}
