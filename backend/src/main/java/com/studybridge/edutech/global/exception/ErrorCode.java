package com.studybridge.edutech.global.exception;

import org.springframework.http.HttpStatus;

/**
 * API에서 사용하는 공통 오류 코드를 정의합니다.
 *
 * <p>HTTP 상태 코드와 서비스 내부 오류 코드를 함께 관리하여
 * Client가 오류의 종류를 명확하게 구분할 수 있도록 합니다.</p>
 */
public enum ErrorCode {

    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "입력값을 확인해주세요."
    ),

    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "EMAIL_ALREADY_EXISTS",
            "이미 사용 중인 이메일입니다."
    ),

    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "INVALID_CREDENTIALS",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    ACCOUNT_NOT_ACTIVE(
            HttpStatus.FORBIDDEN,
            "ACCOUNT_NOT_ACTIVE",
            "현재 사용할 수 없는 계정입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}