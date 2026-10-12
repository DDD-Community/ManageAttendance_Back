package com.ddd.manage_attendance.core.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddd.manage_attendance.domain.auth.domain.UserRole;
import com.ddd.manage_attendance.domain.auth.infrastructure.jwt.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "internal.api-key=0123456789012345678901234567890123456789",
            "jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        })
class InternalApiKeySecurityIntegrationTest {
    private static final String URI = "/api/internal/generations";
    private static final String API_KEY = "0123456789012345678901234567890123456789";

    @Autowired private MockMvc mockMvc;
    @Autowired private TokenProvider tokenProvider;

    @Test
    @DisplayName("내부 API 키가 없으면 401과 내부 키 오류 코드를 반환한다")
    void withoutKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(URI))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_INTERNAL_KEY"));
    }

    @Test
    @DisplayName("잘못된 내부 API 키는 거부한다")
    void wrongKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(URI).header("X-Internal-Api-Key", "wrong-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("올바른 내부 API 키로 기수 목록을 조회한다")
    void validKey_returnsOk() throws Exception {
        mockMvc.perform(get(URI).header("X-Internal-Api-Key", API_KEY)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("운영진 JWT만으로는 내부 API에 접근할 수 없다")
    void managerJwtWithoutKey_returnsUnauthorized() throws Exception {
        final String token = tokenProvider.createAccessToken(1L, UserRole.MANAGER);

        mockMvc.perform(get(URI).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
