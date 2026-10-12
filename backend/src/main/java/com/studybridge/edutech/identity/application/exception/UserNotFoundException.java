package com.studybridge.edutech.identity.application.exception;

/**
 * 요청한 사용자를 찾을 수 없을 때 발생하는 예외입니다.
 *
 * <p>Access Token은 DB 조회 없이 서명과 만료만 검증하므로,
 * 토큰이 유효해도 DB에 사용자가 없을 수 있습니다.
 * (예: 토큰 발급 후 계정 삭제, 개발 DB 초기화)</p>
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("사용자를 찾을 수 없습니다.");
    }
}