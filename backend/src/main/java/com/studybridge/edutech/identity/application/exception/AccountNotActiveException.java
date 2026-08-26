package com.studybridge.edutech.identity.application.exception;

/**
 * 정상적으로 사용할 수 없는 계정으로 로그인하려 할 때 발생합니다.
 */
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException() {
        super("현재 사용할 수 없는 계정입니다.");
    }
}