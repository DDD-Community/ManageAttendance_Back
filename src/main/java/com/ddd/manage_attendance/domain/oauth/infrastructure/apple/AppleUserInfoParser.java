package com.ddd.manage_attendance.domain.oauth.infrastructure.apple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppleUserInfoParser {
    private final ObjectMapper objectMapper;

    /**
     * Apple 로그인 시 전달되는 user 파라미터에서 사용자 이름을 추출합니다.
     *
     * @param userParam Apple로부터 전달받은 user JSON 문자열
     * @return 추출된 사용자 이름 (성 + 이름). 추출 실패 시 null
     */
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
            // user 파싱 실패 시 null 반환
        }

        return null;
    }
}
