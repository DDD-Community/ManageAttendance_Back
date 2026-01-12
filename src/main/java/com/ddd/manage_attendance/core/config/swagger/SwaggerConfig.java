package com.ddd.manage_attendance.core.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class SwaggerConfig {

    @Bean
    public OpenAPI apiConfig() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("출석앱 API")
                                .description(
                                        "DDD 출석앱 SWAGGER UI입니다.\n\n"
                                                + "## 인증 정보\n"
                                                + "- **인증 불필요**: 로그인, 회원가입, 온보딩 API\n"
                                                + "- **JWT 필요**: 마이페이지, 출석 조회 등\n"
                                                + "- **운영진 전용**: 출석 체크/변경, 운영진 관리 기능"));
    }

    @Bean
    public OpenApiCustomizer forceHttpsServerUrl() {
        return openApi ->
                openApi.setServers(List.of(new Server().url("https://api.dddstudy.site")));
    }
}
