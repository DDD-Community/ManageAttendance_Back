package com.ddd.manage_attendance.core.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.ddd.manage_attendance.domain.auth.infrastructure.jwt.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class JwtAuthenticationFilterPathTest {
    @Test
    @DisplayName("JWT 필터는 내부 API만 건너뛴다")
    void skipsInternalOnly() {
        var filter = new JwtAuthenticationFilter(mock(TokenProvider.class), new ObjectMapper());
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/internal/x")))
                .isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/me"))).isFalse();
    }
}
