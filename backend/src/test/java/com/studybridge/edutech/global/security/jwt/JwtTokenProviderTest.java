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
import com.studybridge.edutech.global.security.AuthUser;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    /**
     * 테스트용 Secret: 기존 테스트와 같은 방식(start부터 1씩 증가)으로 32바이트를 만든다.
     * HMAC-SHA256은 최소 256bit(32byte) key가 필요하다.
     */
    private static byte[] secretBytes(int start) {
        byte[] bytes = new byte[32];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (i + start);
        }
        return bytes;
    }

    /**
     * 지정한 Secret과 Access Token 만료 시간으로 Provider를 만든다.
     */
    private static JwtTokenProvider provider(byte[] secret, Duration accessExpiration) {
        return new JwtTokenProvider(
                new JwtProperties(
                        Base64.getEncoder().encodeToString(secret),
                        accessExpiration,
                        Duration.ofDays(14)
                )
        );
    }

    private static User mockUser(UUID userId, Role role) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getRole()).thenReturn(role);
        return user;
    }

    @Test
    @DisplayName("정상 Access Token을 검증하면 사용자 ID와 권한을 꺼낼 수 있다")
    void parseAccessToken_success() {
        // given
        JwtTokenProvider provider = provider(secretBytes(1), Duration.ofMinutes(15));
        UUID userId = UUID.randomUUID();
        String token = provider.generateAccessToken(mockUser(userId, Role.ADMIN));

        // when
        AuthUser authUser = provider.parseAccessToken(token);

        // then
        assertThat(authUser.userId()).isEqualTo(userId);
        assertThat(authUser.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("만료된 Access Token은 거부한다")
    void parseAccessToken_expired() {
        // given: 만료 시간을 -1분으로 → 발급되는 순간 이미 만료된 토큰
        JwtTokenProvider provider = provider(secretBytes(1), Duration.ofMinutes(-1));
        String token = provider.generateAccessToken(mockUser(UUID.randomUUID(), Role.USER));

        // when & then
        assertThatThrownBy(() -> provider.parseAccessToken(token))
                .isInstanceOf(InvalidAccessTokenException.class);
    }

    @Test
    @DisplayName("다른 Secret으로 서명된 Access Token은 거부한다 (위조)")
    void parseAccessToken_invalidSignature() {
        // given: 공격자가 다른 Key로 만든 토큰이라고 가정
        JwtTokenProvider attacker = provider(secretBytes(100), Duration.ofMinutes(15));
        String forgedToken = attacker.generateAccessToken(mockUser(UUID.randomUUID(), Role.ADMIN));

        JwtTokenProvider server = provider(secretBytes(1), Duration.ofMinutes(15));

        // when & then
        assertThatThrownBy(() -> server.parseAccessToken(forgedToken))
                .isInstanceOf(InvalidAccessTokenException.class);
    }

    @Test
    @DisplayName("type이 access가 아닌 토큰은 거부한다")
    void parseAccessToken_wrongType() {
        // given: 서명은 올바르지만 type이 refresh인 토큰을 직접 만든다.
        byte[] secret = secretBytes(1);
        JwtTokenProvider provider = provider(secret, Duration.ofMinutes(15));

        String refreshTypeToken = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("role", "USER")
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(secret))
                .compact();

        // when & then
        assertThatThrownBy(() -> provider.parseAccessToken(refreshTypeToken))
                .isInstanceOf(InvalidAccessTokenException.class);
    }

    @Test
    @DisplayName("형식이 깨진 문자열이나 빈 값은 거부한다")
    void parseAccessToken_malformed() {
        JwtTokenProvider provider = provider(secretBytes(1), Duration.ofMinutes(15));

        assertThatThrownBy(() -> provider.parseAccessToken("abc.def.ghi"))
                .isInstanceOf(InvalidAccessTokenException.class);

        assertThatThrownBy(() -> provider.parseAccessToken(""))
                .isInstanceOf(InvalidAccessTokenException.class);

        assertThatThrownBy(() -> provider.parseAccessToken(null))
                .isInstanceOf(InvalidAccessTokenException.class);
    }
}