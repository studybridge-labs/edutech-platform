package com.studybridge.edutech.global.security.jwt;

import com.studybridge.edutech.identity.domain.Role;
import com.studybridge.edutech.identity.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * JWT Access Token 생성 로직을 검증합니다.
 */
class JwtTokenProviderTest {

    @Test
    @DisplayName("Access Token에는 사용자 ID, 권한, Token 종류가 포함된다")
    void generateAccessToken() {
        // given
        byte[] secretBytes = new byte[32];

        for (int i = 0; i < secretBytes.length; i++) {
            secretBytes[i] = (byte) (i + 1);
        }

        String secret =
                Base64.getEncoder().encodeToString(secretBytes);

        JwtProperties properties = new JwtProperties(
                secret,
                Duration.ofMinutes(15),
                Duration.ofDays(14)
        );

        JwtTokenProvider jwtTokenProvider =
                new JwtTokenProvider(properties);

        UUID userId = UUID.randomUUID();

        User user = mock(User.class);

        when(user.getId())
                .thenReturn(userId);

        when(user.getRole())
                .thenReturn(Role.USER);

        // when
        String accessToken =
                jwtTokenProvider.generateAccessToken(user);

        // then
        Claims claims = Jwts.parser()
                .verifyWith(
                        Keys.hmacShaKeyFor(secretBytes)
                )
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        assertThat(claims.getSubject())
                .isEqualTo(userId.toString());

        assertThat(claims.get("role", String.class))
                .isEqualTo("USER");

        assertThat(claims.get("type", String.class))
                .isEqualTo("access");

        assertThat(claims.getIssuedAt())
                .isNotNull();

        assertThat(claims.getExpiration())
                .isNotNull();

        long expirationSeconds =
                claims.getExpiration().toInstant().getEpochSecond()
                        - claims.getIssuedAt().toInstant().getEpochSecond();

        assertThat(expirationSeconds)
                .isEqualTo(900);
    }
}