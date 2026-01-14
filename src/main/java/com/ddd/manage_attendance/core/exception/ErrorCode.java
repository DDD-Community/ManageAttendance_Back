package com.ddd.manage_attendance.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 인증 관련
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다. 다시 로그인해주세요."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    AUTH_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 가입된 회원 정보입니다"),
    AUTH_GENERATION_MISMATCH(HttpStatus.BAD_REQUEST, "초대 코드의 기수와 요청한 기수가 일치하지 않습니다."),
    AUTH_INVALID_INVITATION_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 초대 코드입니다."),
    AUTH_INVALID_USER_REGISTRATION(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 등록 요청입니다."),

    // 권한 관련
    MANAGER_ONLY(HttpStatus.FORBIDDEN, "운영진 권한이 없는 사용자는 해당 기능을 사용할 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스입니다."),

    // 출석 관련
    ATTENDANCE_ALREADY_CHECKED(HttpStatus.BAD_REQUEST, "이미 출석을 완료했습니다."),
    ATTENDANCE_NOT_USER(HttpStatus.FORBIDDEN, "다른 팀원의 출석을 수정 하고 있습니다."),

    // 스케줄 관련
    SCHEDULE_NOT_ATTENDANCE_DAY(HttpStatus.BAD_REQUEST, "출석일이 아닙니다."),

    // 팀 관련
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다."),

    // 데이터 관련
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 데이터입니다."),

    // OAuth 관련
    OAUTH_TOKEN_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "OAuth 토큰 검증에 실패했습니다."),
    OAUTH_USER_INFO_NOT_FOUND(HttpStatus.BAD_REQUEST, "OAuth 사용자 정보를 가져올 수 없습니다."),
    OAUTH_USER_ID_NOT_FOUND(HttpStatus.BAD_REQUEST, "OAuth 사용자 ID(sub)가 없습니다."),
    OAUTH_TOKEN_ISSUE_FAILED(HttpStatus.BAD_REQUEST, "OAuth 토큰 발급에 실패했습니다."),
    OAUTH_REVOKE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OAuth 인증 철회 중 오류가 발생했습니다."),
    OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_REQUEST, "OAuth 토큰 교환에 실패했습니다."),
    OAUTH_KEY_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OAuth 키 파싱에 실패했습니다."),
    OAUTH_UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth 제공자입니다."),

    // 검증 관련
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),
    BIND_ERROR(HttpStatus.BAD_REQUEST, "요청 데이터 바인딩에 실패했습니다."),

    // 시스템 관련
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
