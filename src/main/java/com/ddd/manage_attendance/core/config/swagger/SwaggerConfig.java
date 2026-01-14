package com.ddd.manage_attendance.core.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
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
                                .description("DDD 출석앱 SWAGGER UI입니다."));
    }

    @Bean
    public OpenApiCustomizer forceHttpsServerUrl() {
        return openApi ->
                openApi.setServers(List.of(new Server().url("https://api.dddstudy.site")));
    }

    @Bean
    public OpenApiCustomizer addCommonErrorResponses() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths()
                        .values()
                        .forEach(
                                pathItem ->
                                        pathItem.readOperations()
                                                .forEach(
                                                        operation -> {
                                                            ApiResponses responses =
                                                                    operation.getResponses();

                                                            // 400 Bad Request
                                                            responses.addApiResponse(
                                                                    "400",
                                                                    createErrorResponse(
                                                                            "Bad Request",
                                                                            "잘못된 요청",
                                                                            "VALIDATION_ERROR",
                                                                            "입력값 검증에 실패했습니다."));

                                                            // 401 Unauthorized
                                                            responses.addApiResponse(
                                                                    "401",
                                                                    createErrorResponse(
                                                                            "Unauthorized",
                                                                            "인증 실패",
                                                                            "AUTH_EXPIRED_TOKEN",
                                                                            "만료된 토큰입니다. 다시 로그인해주세요."));

                                                            // 403 Forbidden
                                                            responses.addApiResponse(
                                                                    "403",
                                                                    createErrorResponse(
                                                                            "Forbidden",
                                                                            "권한 부족",
                                                                            "MANAGER_ONLY",
                                                                            "운영진 권한이 없는 사용자는 해당 기능을 사용할 수 없습니다."));

                                                            // 404 Not Found
                                                            responses.addApiResponse(
                                                                    "404",
                                                                    createErrorResponse(
                                                                            "Not Found",
                                                                            "리소스 없음",
                                                                            "DATA_NOT_FOUND",
                                                                            "존재하지 않는 데이터입니다."));

                                                            // 500 Internal Server Error
                                                            responses.addApiResponse(
                                                                    "500",
                                                                    createErrorResponse(
                                                                            "Internal Server Error",
                                                                            "서버 오류",
                                                                            "INTERNAL_SERVER_ERROR",
                                                                            "서버 내부 오류가 발생했습니다."));
                                                        }));
            }
        };
    }

    private ApiResponse createErrorResponse(
            String description, String summary, String errorCode, String errorMessage) {
        MediaType mediaType = new MediaType();

        Schema<Object> schema =
                new Schema<>()
                        .type("object")
                        .description("표준 에러 응답")
                        .addProperty(
                                "code", new StringSchema().description("에러 코드").example(errorCode))
                        .addProperty(
                                "message",
                                new StringSchema().description("에러 메시지").example(errorMessage))
                        .addProperty(
                                "detail",
                                new StringSchema().description("상세 정보").example("필드 유효성 검증 실패"));

        mediaType.setSchema(schema);

        Example example = new Example();
        example.setSummary(summary);
        example.setValue(
                String.format(
                        "{\"code\":\"%s\",\"message\":\"%s\",\"detail\":null}",
                        errorCode, errorMessage));

        mediaType.addExamples("example", example);

        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", mediaType));
    }
}
