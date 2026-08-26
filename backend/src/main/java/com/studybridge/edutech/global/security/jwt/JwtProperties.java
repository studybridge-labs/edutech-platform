package com.studybridge.edutech.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 인증 Token 발급에 필요한 설정값을 관리합니다.
 *
 * <p>JWT Secret과 Access/Refresh Token의 만료 시간을
 * application.yaml 및 환경변수에서 주입받습니다.</p>
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {
}