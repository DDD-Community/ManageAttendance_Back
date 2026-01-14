
package com.ddd.manage_attendance.domain.oauth.exception;

import com.ddd.manage_attendance.core.exception.BaseException;
import com.ddd.manage_attendance.core.exception.ErrorCode;

public class OAuthServiceException extends BaseException {

    public OAuthServiceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OAuthServiceException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public OAuthServiceException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode, customMessage, cause);
    }

    public OAuthServiceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
