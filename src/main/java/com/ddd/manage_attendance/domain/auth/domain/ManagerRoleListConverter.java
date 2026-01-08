package com.ddd.manage_attendance.domain.auth.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

@Converter
public class ManagerRoleListConverter implements AttributeConverter<List<ManagerRole>, String> {

    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(List<ManagerRole> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream().map(ManagerRole::name).collect(Collectors.joining(SPLIT_CHAR));
    }

    @Override
    public List<ManagerRole> convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return new ArrayList<>();
        }
        return Arrays.stream(dbData.split(SPLIT_CHAR))
                .map(ManagerRole::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
