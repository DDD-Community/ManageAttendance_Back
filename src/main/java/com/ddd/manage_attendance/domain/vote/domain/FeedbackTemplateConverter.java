package com.ddd.manage_attendance.domain.vote.domain;

import com.ddd.manage_attendance.core.sdui.FeedbackTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;

/** {@link FeedbackTemplate} 을 text 컬럼에 JSON 으로 직렬화/역직렬화한다. (기존 @Convert 선례 준수) */
@Converter
public class FeedbackTemplateConverter implements AttributeConverter<FeedbackTemplate, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(final FeedbackTemplate attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("피드백 템플릿 직렬화에 실패했습니다.", e);
        }
    }

    @Override
    public FeedbackTemplate convertToEntityAttribute(final String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return null;
        }
        try {
            return MAPPER.readValue(dbData, FeedbackTemplate.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("피드백 템플릿 역직렬화에 실패했습니다.", e);
        }
    }
}
