package com.ddd.manage_attendance.domain.oauth.infrastructure.apple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppleUserInfoParser {
    private final ObjectMapper objectMapper;

    public String extractUserName(String userParam) {
        if (userParam == null || userParam.trim().isEmpty()) {
            return null;
        }

        try {
            JsonNode userJson = objectMapper.readTree(userParam);
            JsonNode nameNode = userJson.get("name");

            if (nameNode != null) {
                String firstName =
                        nameNode.has("firstName") ? nameNode.get("firstName").asText() : "";
                String lastName = nameNode.has("lastName") ? nameNode.get("lastName").asText() : "";
                String userName = (lastName + " " + firstName).trim();
                return userName.isEmpty() ? null : userName;
            }
        } catch (Exception e) {
        }

        return null;
    }
}
