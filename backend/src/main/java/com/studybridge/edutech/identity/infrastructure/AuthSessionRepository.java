package com.studybridge.edutech.identity.infrastructure;

import com.studybridge.edutech.identity.domain.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * 로그인 Session 영속성을 관리합니다.
 */
public interface AuthSessionRepository
        extends JpaRepository<AuthSession, UUID> {

    /**
     * Refresh Token Hash를 기준으로 Session을 조회합니다.
     */
    Optional<AuthSession> findByRefreshTokenHash(
            String refreshTokenHash
    );
}