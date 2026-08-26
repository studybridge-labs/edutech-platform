package com.studybridge.edutech.identity.domain;

import com.studybridge.edutech.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * 사용자의 로그인 세션을 관리합니다.
 *
 * <p>Refresh Token 원문은 저장하지 않고,
 * SHA-256으로 변환한 Hash 값만 저장합니다.</p>
 *
 * <p>Access Token은 Stateless JWT로 사용하고,
 * Refresh Token만 서버에서 관리하여 재발급과 로그아웃을 제어합니다.</p>
 */
@Entity
@Table(
        name = "auth_sessions",
        indexes = {
                @Index(
                        name = "idx_auth_sessions_refresh_token_hash",
                        columnList = "refresh_token_hash"
                ),
                @Index(
                        name = "idx_auth_sessions_user_id",
                        columnList = "user_id"
                )
        }
)
public class AuthSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_auth_sessions_user")
    )
    private User user;

    /**
     * Refresh Token 원문의 SHA-256 Hash입니다.
     */
    @Column(
            name = "refresh_token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String refreshTokenHash;

    /**
     * Refresh Token 만료 시각입니다.
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * 로그아웃 또는 Refresh Token 폐기 시각입니다.
     *
     * null이면 아직 폐기되지 않은 Session입니다.
     */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected AuthSession() {
    }

    private AuthSession(
            User user,
            String refreshTokenHash,
            Instant expiresAt
    ) {
        this.user = user;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
    }

    /**
     * 새로운 로그인 Session을 생성합니다.
     */
    public static AuthSession create(
            User user,
            String refreshTokenHash,
            Instant expiresAt
    ) {
        return new AuthSession(
                user,
                refreshTokenHash,
                expiresAt
        );
    }

    /**
     * 로그인 Session을 폐기합니다.
     */
    public void revoke(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    /**
     * Session이 이미 폐기되었는지 확인합니다.
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /**
     * Refresh Token이 만료되었는지 확인합니다.
     */
    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }
}