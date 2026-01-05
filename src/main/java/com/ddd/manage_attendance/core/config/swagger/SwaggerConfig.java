package com.ddd.manage_attendance.core.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
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
                .info(new Info().title("출석앱 API").description("DDD 출석앱 SWAGGER UI입니다."))
                .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }

    @Bean
    public OpenApiCustomizer forceHttpsServerUrl() {
        return openApi ->
                openApi.setServers(List.of(new Server().url("https://api.dddstudy.site")));
    }
}
