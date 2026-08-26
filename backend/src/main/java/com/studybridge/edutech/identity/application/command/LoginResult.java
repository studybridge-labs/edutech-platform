package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.identity.api.command.dto.LoginResponse;

/**
 * 로그인 Application Service의 내부 결과입니다.
 *
 * <p>Refresh Token 원문은 Controller에서
 * HttpOnly Cookie로 전달하기 위해 별도로 보관합니다.</p>
 */
public record LoginResult(
        LoginResponse response,
        String refreshToken
) {
}