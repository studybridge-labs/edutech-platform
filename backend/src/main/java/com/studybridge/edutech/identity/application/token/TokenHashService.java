package com.studybridge.edutech.identity.application.token;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Refresh Token 원문을 SHA-256 Hash로 변환합니다.
 *
 * <p>DB에는 Refresh Token 원문을 저장하지 않고
 * SHA-256 Hash 값만 저장합니다.</p>
 */
@Component
public class TokenHashService {

    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Refresh Token을 SHA-256 Hash 문자열로 변환합니다.
     *
     * @param token Refresh Token 원문
     * @return 64자리 hexadecimal SHA-256 Hash
     */
    public String hash(String token) {
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance(HASH_ALGORITHM);

            byte[] digest = messageDigest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException exception) {
            /**
             * SHA-256은 Java 표준 구현에서 반드시 지원되므로
             * 발생한다면 애플리케이션 구성 자체의 심각한 문제입니다.
             */
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    exception
            );
        }
    }
}