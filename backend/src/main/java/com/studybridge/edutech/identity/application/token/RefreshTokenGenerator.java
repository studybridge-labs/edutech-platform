package com.studybridge.edutech.identity.application.token;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Refresh Token 원문을 생성합니다.
 *
 * <p>Refresh Token은 사용자가 예측할 수 없어야 하므로
 * 일반 Random이 아닌 암호학적으로 안전한 SecureRandom을 사용합니다.</p>
 */
@Component
public class RefreshTokenGenerator {

    /**
     * 256-bit 랜덤 값을 생성합니다.
     */
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * URL과 Cookie에서 안전하게 사용할 수 있는
     * Base64 URL 형식의 Refresh Token을 생성합니다.
     *
     * @return 새 Refresh Token 원문
     */
    public String generate() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}