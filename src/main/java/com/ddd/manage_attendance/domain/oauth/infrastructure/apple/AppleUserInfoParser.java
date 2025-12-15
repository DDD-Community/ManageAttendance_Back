package com.ddd.manage_attendance.domain.oauth.infrastructure.apple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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
            log.debug("[AppleUserInfoParser] user 파라미터가 비어있음 (재로그인)");
            return null;
        }

        try {
            log.debug("[AppleUserInfoParser] user 파라미터 파싱 시작: {}", userParam);
            JsonNode userJson = objectMapper.readTree(userParam);
            JsonNode nameNode = userJson.get("name");

            if (nameNode != null) {
                String firstName =
                        nameNode.has("firstName") ? nameNode.get("firstName").asText() : "";
                String lastName = nameNode.has("lastName") ? nameNode.get("lastName").asText() : "";
                String userName = (lastName + " " + firstName).trim();
                log.info(
                        "[AppleUserInfoParser] 사용자 이름 파싱 성공 - firstName: {}, lastName: {}, 결과: {}",
                        firstName,
                        lastName,
                        userName);
                return userName.isEmpty() ? null : userName;
            } else {
                log.warn("[AppleUserInfoParser] user 파라미터에 name 필드가 없음");
            }
        } catch (Exception e) {
            log.error("[AppleUserInfoParser] user 파싱 실패", e);
        }

        return null;
    }
}
