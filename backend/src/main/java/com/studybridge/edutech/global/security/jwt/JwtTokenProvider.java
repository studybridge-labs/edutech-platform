package com.studybridge.edutech.global.security.jwt;

import com.studybridge.edutech.identity.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

/**
 * JWT Access Token을 생성합니다.
 *
 * <p>Access Token에는 인증 및 인가에 필요한 최소 정보만 저장합니다.</p>
 */
@Component
public class JwtTokenProvider {

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
                .claim("type", "access")

                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))

                /**
                 * JWT가 서버에서 발급된 Token임을 검증할 수 있도록
                 * Secret Key로 서명합니다.
                 */
                .signWith(signingKey)

                .compact();
    }
}