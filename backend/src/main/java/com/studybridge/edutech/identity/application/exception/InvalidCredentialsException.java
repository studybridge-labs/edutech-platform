package com.studybridge.edutech.identity.application.exception;

/**
 * 이메일 또는 비밀번호가 올바르지 않을 때 발생합니다.
 *
 * <p>이메일 존재 여부를 외부에 노출하지 않기 위해
 * 동일한 오류 메시지를 사용합니다.</p>
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}