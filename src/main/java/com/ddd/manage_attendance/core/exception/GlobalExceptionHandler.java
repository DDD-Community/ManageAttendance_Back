package com.ddd.manage_attendance.core.exception;

import com.ddd.manage_attendance.domain.oauth.exception.OAuthTokenValidationException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OAuthTokenValidationException.class)
    public ResponseEntity<ErrorResponse> handleOAuthTokenValidationException(
            OAuthTokenValidationException e, HttpServletRequest request) {
        log.error(
                "OAuth Validation Failed. Provider: {}, Message: {}, Path: {}",
                e.getProvider(),
                e.getMessage(),
                request.getRequestURI(),
                e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("OAUTH_ERROR", e.getMessage(), getStackTrace(e)));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException e, HttpServletRequest request) {
        log.warn(
                "Business Exception. Message: {}, Path: {}",
                e.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("BAD_REQUEST", e.getMessage(), getStackTrace(e)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation Failed. Message: {}, Path: {}", message, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", message, getStackTrace(e)));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Bind Failed. Message: {}, Path: {}", message, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("BIND_ERROR", message, getStackTrace(e)));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
         log.warn("Access Denied. Message: {}, Path: {}", e.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("UNAUTHORIZED", "로그인이 필요한 서비스입니다.", getStackTrace(e)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error(
                "Unexpected Error. Message: {}, Path: {}",
                e.getMessage(),
                request.getRequestURI(),
                e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_SERVER_ERROR", e.getMessage(), getStackTrace(e)));
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
