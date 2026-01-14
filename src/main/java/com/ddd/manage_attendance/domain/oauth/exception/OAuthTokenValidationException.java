package com.ddd.manage_attendance.domain.oauth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;
import lombok.Getter;

@Getter
public class OAuthTokenValidationException extends BaseException {
    private final String provider;

    public OAuthTokenValidationException(String provider, String message) {
        super(ErrorCode.OAUTH_TOKEN_VALIDATION_FAILED, message);
        this.provider = provider;
    }

    public OAuthTokenValidationException(String provider, String message, Throwable cause) {
        super(ErrorCode.OAUTH_TOKEN_VALIDATION_FAILED, message, cause);
        this.provider = provider;
    }

    @Override
    public String getMessage() {
        return String.format("[%s OAuth] %s", provider, super.getMessage());
    }
}
