package com.ddd.manage_attendance.core.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class InternalApiKeyFilterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("정상 키는 내부 권한으로 요청을 진행한다")
    void acceptsValidKey() throws Exception {
        var filter = new InternalApiKeyFilter("a".repeat(32), objectMapper);
        var request = request();
        request.addHeader("X-Internal-Api-Key", "a".repeat(32));
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .contains("INTERNAL");
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("틀린 키와 헤더 누락은 401을 반환한다")
    void rejectsInvalidKey() throws Exception {
        for (String key : new String[] {"wrong", null}) {
            var filter = new InternalApiKeyFilter("a".repeat(32), objectMapper);
            var request = request();
            if (key != null) request.addHeader("X-Internal-Api-Key", key);
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("AUTH_INVALID_INTERNAL_KEY");
        }
    }

    @Test
    @DisplayName("설정 키가 비었거나 짧으면 인증을 허용하지 않는다")
    void rejectsMisconfiguredKey() throws Exception {
        for (String key : new String[] {"", "a".repeat(31)}) {
            var filter = new InternalApiKeyFilter(key, objectMapper);
            var request = request();
            request.addHeader("X-Internal-Api-Key", "a".repeat(32));
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            assertThat(response.getStatus()).isEqualTo(401);
        }
    }

    @Test
    @DisplayName("내부 경로가 아니면 필터를 건너뛴다")
    void skipsNonInternalPath() {
        assertThat(
                        new InternalApiKeyFilter("a".repeat(32), objectMapper)
                                .shouldNotFilter(new MockHttpServletRequest("GET", "/api/me")))
                .isTrue();
    }

    private MockHttpServletRequest request() {
        return new MockHttpServletRequest("GET", "/api/internal/generations");
    }
}
