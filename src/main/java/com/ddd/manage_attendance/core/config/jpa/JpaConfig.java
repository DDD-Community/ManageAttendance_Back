package com.ddd.manage_attendance.core.config.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
class JpaConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        // OAuth 로그인 시 시스템 사용자 ID를 반환
        // TODO: 실제 인증된 사용자 ID를 반환하도록 수정 필요
        return () -> java.util.Optional.of(0L);
    }
}
