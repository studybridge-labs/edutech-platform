package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.identity.application.token.TokenHashService;
import com.studybridge.edutech.identity.infrastructure.AuthSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Refresh Token에 연결된 인증 세션을 종료합니다.
 */
@Service
public class LogoutService {

    private final AuthSessionRepository authSessionRepository;
    private final TokenHashService tokenHashService;

    public LogoutService(
            AuthSessionRepository authSessionRepository,
            TokenHashService tokenHashService
    ) {
        this.authSessionRepository = authSessionRepository;
        this.tokenHashService = tokenHashService;
    }

    /**
     * 로그아웃은 반복 호출되어도 안전하도록 처리합니다.
     *
     * Refresh Token이 없거나 이미 유효하지 않은 경우에도
     * 별도의 예외를 발생시키지 않습니다.
     */
    @Transactional
    public void logout(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String refreshTokenHash =
                tokenHashService.hash(refreshToken);

        authSessionRepository
                .findByRefreshTokenHash(refreshTokenHash)
                .ifPresent(session -> {
                    if (!session.isRevoked()) {
                        session.revoke(Instant.now());
                    }
                });
    }
}