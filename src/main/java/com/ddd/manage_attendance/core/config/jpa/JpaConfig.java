package com.ddd.manage_attendance.core.config.jpa;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing
class JpaConfig {

    // 인증 컨텍스트가 없는 경우(OAuth 가입 등 시스템 작업)에 사용하는 감사자 ID
    private static final Long SYSTEM_AUDITOR = 0L;

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            final Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of(SYSTEM_AUDITOR);
            }
            // JwtAuthenticationFilter 가 principal 로 Long userId 를 설정한다.
            if (authentication.getPrincipal() instanceof Long userId) {
                return Optional.of(userId);
            }
            return Optional.of(SYSTEM_AUDITOR);
        };
    }
}
