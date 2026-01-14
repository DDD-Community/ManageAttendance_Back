package com.ddd.manage_attendance.core.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
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
                                .description(
                                        "DDD 출석앱 SWAGGER UI입니다.\n\n"
                                                + "# 인증 및 회원가입 플로우\n\n"
                                                + "## 1. 신규 사용자 회원가입 플로우\n\n"
                                                + "### Step 1: OAuth 로그인 시도\n"
                                                + "```\n"
                                                + "POST /api/auth/login\n"
                                                + "{\n"
                                                + "  \"provider\": \"GOOGLE\" 또는 \"APPLE\",\n"
                                                + "  \"token\": \"OAuth ID Token\"\n"
                                                + "}\n"
                                                + "```\n"
                                                + "**응답 202**: 신규 사용자 → 회원가입 필요\n"
                                                + "- `isNewUser: true`\n"
                                                + "- `oauthRefreshToken`: 회원가입 시 사용\n\n"
                                                + "### Step 2: 초대 코드 검증\n"
                                                + "```\n"
                                                + "GET /api/onboarding/verify-code?code={초대코드}\n"
                                                + "```\n"
                                                + "**응답**: generationId, generationName\n\n"
                                                + "### Step 3: 온보딩 정보 조회\n"
                                                + "```\n"
                                                + "GET /api/onboarding/jobs          # 직군 목록\n"
                                                + "GET /api/onboarding/teams?generationId={기수ID}  # 팀 목록\n"
                                                + "GET /api/onboarding/manager-roles # 매니저 역할 (매니저인 경우)\n"
                                                + "```\n\n"
                                                + "### Step 4: 회원가입 완료\n"
                                                + "```\n"
                                                + "POST /api/users\n"
                                                + "{\n"
                                                + "  \"name\": \"홍길동\",\n"
                                                + "  \"generationId\": 1,\n"
                                                + "  \"jobRole\": \"BACKEND\",\n"
                                                + "  \"teamId\": 1,\n"
                                                + "  \"managerRoles\": [],\n"
                                                + "  \"provider\": \"GOOGLE\",\n"
                                                + "  \"token\": \"OAuth ID Token\",\n"
                                                + "  \"oauthRefreshToken\": \"Step 1에서 받은 토큰\",\n"
                                                + "  \"invitationCode\": \"초대코드\"\n"
                                                + "}\n"
                                                + "```\n"
                                                + "**응답 200**: 회원가입 성공 → 사용자 정보 반환\n\n"
                                                + "---\n\n"
                                                + "## 2. 기존 사용자 로그인 플로우\n\n"
                                                + "### Step 1: OAuth 로그인\n"
                                                + "```\n"
                                                + "POST /api/auth/login\n"
                                                + "{\n"
                                                + "  \"provider\": \"GOOGLE\" 또는 \"APPLE\",\n"
                                                + "  \"token\": \"OAuth ID Token\"\n"
                                                + "}\n"
                                                + "```\n"
                                                + "**응답 201**: 로그인 성공\n"
                                                + "- `isNewUser: false`\n"
                                                + "- `accessToken`: API 호출 시 사용 (유효기간: 24시간)\n"
                                                + "- `refreshToken`: 토큰 재발급 시 사용\n\n"
                                                + "### Step 2: 인증이 필요한 API 호출\n"
                                                + "```\n"
                                                + "Authorization: Bearer {accessToken}\n"
                                                + "```\n\n"
                                                + "---\n\n"
                                                + "## 3. 토큰 관리\n\n"
                                                + "### Access Token 만료 시 재발급\n"
                                                + "```\n"
                                                + "POST /api/auth/refresh\n"
                                                + "{\n"
                                                + "  \"refreshToken\": \"Refresh Token\"\n"
                                                + "}\n"
                                                + "```\n"
                                                + "**응답**: 새로운 accessToken, refreshToken\n\n"
                                                + "### 로그아웃\n"
                                                + "```\n"
                                                + "POST /api/auth/logout\n"
                                                + "Authorization: Bearer {accessToken}\n"
                                                + "```\n\n"
                                                + "---\n\n"
                                                + "# HTTP Status Code 정책\n\n"
                                                + "## 인증 관련 특수 상태 코드\n"
                                                + "- **201 Created**: 기존 회원 로그인 성공\n"
                                                + "- **202 Accepted**: 신규 사용자 (회원가입 필요)\n"
                                                + "- **200 OK**: 일반 성공 응답\n\n"
                                                + "## 일반 상태 코드\n"
                                                + "- **400 Bad Request**: 잘못된 요청 (필수값 누락, 검증 실패 등)\n"
                                                + "- **401 Unauthorized**: 인증 실패 (토큰 만료, 유효하지 않은 토큰)\n"
                                                + "- **403 Forbidden**: 권한 부족 (운영진 전용 API 등)\n"
                                                + "- **404 Not Found**: 리소스 없음\n"
                                                + "- **500 Internal Server Error**: 서버 오류\n\n"
                                                + "---\n\n"
                                                + "# 에러 응답 형식\n\n"
                                                + "모든 에러는 다음 형식으로 응답됩니다:\n"
                                                + "```json\n"
                                                + "{\n"
                                                + "  \"code\": \"ERROR_CODE_NAME\",\n"
                                                + "  \"message\": \"사용자에게 표시할 메시지\",\n"
                                                + "  \"detail\": \"상세 정보 (선택적)\"\n"
                                                + "}\n"
                                                + "```\n\n"
                                                + "## 주요 에러 코드\n\n"
                                                + "### 인증 관련 (401)\n"
                                                + "- `AUTH_EXPIRED_TOKEN`: 토큰 만료 → 재로그인 또는 토큰 재발급 필요\n"
                                                + "- `AUTH_INVALID_TOKEN`: 유효하지 않은 토큰\n"
                                                + "- `UNAUTHORIZED`: 로그인 필요\n\n"
                                                + "### 회원가입 관련 (400)\n"
                                                + "- `AUTH_ALREADY_REGISTERED`: 이미 가입된 회원\n"
                                                + "- `AUTH_GENERATION_MISMATCH`: 초대 코드와 기수 불일치\n"
                                                + "- `AUTH_INVALID_INVITATION_CODE`: 유효하지 않은 초대 코드\n"
                                                + "- `AUTH_INVALID_USER_REGISTRATION`: 잘못된 회원가입 요청\n\n"
                                                + "### 권한 관련 (403)\n"
                                                + "- `MANAGER_ONLY`: 운영진 전용 기능\n"
                                                + "- `ATTENDANCE_NOT_USER`: 다른 팀원의 출석 수정 불가\n\n"
                                                + "### 출석 관련 (400)\n"
                                                + "- `ATTENDANCE_ALREADY_CHECKED`: 이미 출석 완료\n"
                                                + "- `SCHEDULE_NOT_ATTENDANCE_DAY`: 출석일이 아님\n\n"
                                                + "### 데이터 관련 (404)\n"
                                                + "- `DATA_NOT_FOUND`: 존재하지 않는 데이터\n"
                                                + "- `TEAM_NOT_FOUND`: 존재하지 않는 팀\n\n"
                                                + "### OAuth 관련 (400/500)\n"
                                                + "- `OAUTH_TOKEN_VALIDATION_FAILED`: OAuth 토큰 검증 실패\n"
                                                + "- `OAUTH_USER_ID_NOT_FOUND`: OAuth 사용자 ID(sub) 없음\n"
                                                + "- `OAUTH_UNSUPPORTED_PROVIDER`: 지원하지 않는 OAuth 제공자\n\n"
                                                + "### 검증 관련 (400)\n"
                                                + "- `VALIDATION_ERROR`: 입력값 검증 실패\n"
                                                + "- `BIND_ERROR`: 요청 데이터 바인딩 실패\n\n"
                                                + "---\n\n"
                                                + "# 인증 권한 정보\n\n"
                                                + "- **인증 불필요**: 로그인, 회원가입, 온보딩 API, QR 조회\n"
                                                + "- **JWT 필요**: 마이페이지, 출석 조회, 내 정보 수정/탈퇴\n"
                                                + "- **운영진 전용**: 출석 체크/변경, 운영진 관리 기능\n\n"
                                                + "---\n\n"
                                                + "# OAuth 토큰 사용 가이드\n\n"
                                                + "## Google OAuth\n"
                                                + "- **권장**: `id_token` 사용\n"
                                                + "- id_token에는 사용자 정보(sub, email 등)가 포함되어 있어 서버에서 직접 검증 가능\n"
                                                + "- 토큰 형식: JWT (eyJ로 시작)\n\n"
                                                + "## Apple OAuth\n"
                                                + "- **필수**: `identity token` 사용\n"
                                                + "- Apple의 공개키로 검증\n"
                                                + "- 토큰 형식: JWT (eyJ로 시작)\n\n"
                                                + "---\n\n"
                                                + "## FAQ\n\n"
                                                + "**Q: 로그인 후 어떤 상태 코드를 받아야 하나요?**\n"
                                                + "A: 기존 회원은 201, 신규 사용자는 202를 받습니다.\n\n"
                                                + "**Q: 신규 사용자인 경우 어떻게 해야 하나요?**\n"
                                                + "A: 202 응답 시 온보딩 플로우를 진행하여 회원가입을 완료하세요.\n\n"
                                                + "**Q: Access Token이 만료되면 어떻게 하나요?**\n"
                                                + "A: Refresh Token으로 `/api/auth/refresh`를 호출하여 새로운 토큰을 발급받으세요.\n\n"
                                                + "**Q: 매니저와 일반 멤버의 차이는?**\n"
                                                + "A: 매니저는 teamId가 null 가능하고 managerRoles를 가지며, 운영진 전용 기능 사용이 가능합니다."));
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
        mediaType.setSchema(new Schema<>().$ref("#/components/schemas/ErrorResponse"));

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
