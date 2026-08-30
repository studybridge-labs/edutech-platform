package com.studybridge.edutech.identity.application.exception;

/**
 * Refresh Token이 없거나,
 * 만료되었거나,
 * 이미 폐기된 경우 발생합니다.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("유효하지 않은 인증 세션입니다.");
    }
}