package com.studybridge.edutech.identity.application.exception;

/**
 * 이미 사용 중인 이메일로 회원가입을 시도했을 때 발생하는 예외입니다.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("이미 사용 중인 이메일입니다.");
    }
}