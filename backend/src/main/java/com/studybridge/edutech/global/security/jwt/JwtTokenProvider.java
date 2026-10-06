package com.studybridge.edutech.global.security.jwt;

import com.studybridge.edutech.global.security.AuthUser;     // ★ 추가
import com.studybridge.edutech.identity.domain.Role;          // ★ 추가
import com.studybridge.edutech.identity.domain.User;
import io.jsonwebtoken.Claims;                                // ★ 추가
import io.jsonwebtoken.JwtException;                          // ★ 추가
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;                                        // ★ 추가

/**
 * JWT Access Token을 생성하고 검증합니다.
 *
 * <p>Access Token에는 인증 및 인가에 필요한 최소 정보만 저장합니다.</p>
 */
@Component
public class JwtTokenProvider {

    /**
     * ★ 추가: Token 종류를 나타내는 claim 값.
     * 생성과 검증에서 같은 값을 쓰도록 상수로 묶는다. (오타 방지)
     */
    private static final String TOKEN_TYPE_ACCESS = "access";

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    /**
     * Base64로 인코딩된 JWT Secret을 HMAC 서명 Key로 변환합니다.
     */
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        byte[] keyBytes = Decoders.BASE64.decode(
                jwtProperties.secret()
        );

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 로그인 사용자의 Access Token을 생성합니다.
     *
     * @param user 인증된 사용자
     * @return JWT Access Token
     */
    public String generateAccessToken(User user) {
        Instant issuedAt = Instant.now();

        Instant expiresAt = issuedAt.plus(
                jwtProperties.accessTokenExpiration()
        );

        return Jwts.builder()
                /**
                 * JWT의 주체입니다.
                 * 이메일 대신 변경되지 않는 User UUID를 사용합니다.
                 */
                .subject(user.getId().toString())

                /**
                 * Authorization 처리에 사용할 권한입니다.
                 */
                .claim("role", user.getRole().name())

                /**
                 * Refresh Token 등 다른 Token과 구별하기 위한 Claim입니다.
                 */
                .claim("type", TOKEN_TYPE_ACCESS)          // ★ 변경: "access" → 상수

                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))

                /**
                 * JWT가 서버에서 발급된 Token임을 검증할 수 있도록
                 * Secret Key로 서명합니다.
                 */
                .signWith(signingKey)

                .compact();
    }

    /**
     * ★ 추가
     * Access Token을 검증하고, 토큰에 담긴 사용자 정보를 꺼냅니다.
     *
     * <p>검증 순서</p>
     * <ol>
     *   <li>서명 검증: 우리 서버의 Secret Key로 서명된 토큰인가? (위조 여부)</li>
     *   <li>만료 검증: expiration 시간이 지나지 않았는가?</li>
     *   <li>종류 검증: type claim이 "access" 인가?</li>
     *   <li>필수 값 검증: subject(userId), role 이 올바른 형식인가?</li>
     * </ol>
     *
     * @param token Authorization 헤더에서 "Bearer " 를 뗀 순수 토큰 문자열
     * @return 토큰의 사용자 정보
     * @throws InvalidAccessTokenException 위 검증 중 하나라도 실패한 경우
     */
    public AuthUser parseAccessToken(String token) {
        try {
            /*
             * 1, 2번 검증은 JJWT가 parseSignedClaims() 안에서 한 번에 처리한다.
             * - 서명이 다르면       → SignatureException
             * - 만료됐으면          → ExpiredJwtException
             * - 형식이 깨졌으면     → MalformedJwtException
             * - null / 빈 문자열이면 → IllegalArgumentException
             * (앞의 세 개는 모두 JwtException의 하위 클래스)
             */
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 3. 종류 검증
            String type = claims.get("type", String.class);

            if (!TOKEN_TYPE_ACCESS.equals(type)) {
                // "access".equals(type) 순서로 쓰면 type이 null이어도 NPE가 나지 않는다.
                throw new InvalidAccessTokenException("Access Token이 아닙니다.");
            }

            // 4. 필수 값 검증
            String subject = claims.getSubject();
            String role = claims.get("role", String.class);

            if (subject == null || role == null) {
                // Role.valueOf(null)은 NPE를 던지므로 미리 막는다.
                throw new InvalidAccessTokenException("Access Token에 필수 정보가 없습니다.");
            }

            return new AuthUser(
                    UUID.fromString(subject),   // UUID 형식이 아니면 IllegalArgumentException
                    Role.valueOf(role)          // USER/ADMIN 이 아니면 IllegalArgumentException
            );

        } catch (JwtException | IllegalArgumentException e) {
            /*
             * JJWT 예외와 형식 변환 예외를 하나의 예외로 감싼다.
             * 원래 예외(e)는 cause로 보관해서 디버깅할 때 원인을 볼 수 있게 한다.
             */
            throw new InvalidAccessTokenException("유효하지 않은 Access Token입니다.", e);
        }
    }
}