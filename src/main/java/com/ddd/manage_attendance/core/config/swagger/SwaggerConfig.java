package com.ddd.manage_attendance.core.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
class SwaggerConfig {

    @Bean
    public OpenAPI apiConfig() {
        return new OpenAPI()
                .info(new Info().title("출석앱 API").description("DDD 출석앱 SWAGGER UI입니다."));
    }
}
