package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.identity.application.token.TokenHashService;
import com.studybridge.edutech.identity.domain.AuthSession;
import com.studybridge.edutech.identity.infrastructure.AuthSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 로그아웃 서비스 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private TokenHashService tokenHashService;

    @Mock
    private AuthSession authSession;

    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutService(
                authSessionRepository,
                tokenHashService
        );
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 로그아웃하면 세션을 revoke한다")
    void logoutSuccess() {
        // given
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";

        when(tokenHashService.hash(refreshToken))
                .thenReturn(refreshTokenHash);

        when(
                authSessionRepository.findByRefreshTokenHash(
                        refreshTokenHash
                )
        )
                .thenReturn(Optional.of(authSession));

        when(authSession.isRevoked())
                .thenReturn(false);

        // when
        logoutService.logout(refreshToken);

        // then
        verify(tokenHashService)
                .hash(refreshToken);

        verify(authSessionRepository)
                .findByRefreshTokenHash(refreshTokenHash);

        verify(authSession)
                .revoke(any(Instant.class));
    }

    @Test
    @DisplayName("이미 revoke된 세션으로 로그아웃해도 다시 revoke하지 않는다")
    void logoutDoesNothingWhenAlreadyRevoked() {
        // given
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";

        when(tokenHashService.hash(refreshToken))
                .thenReturn(refreshTokenHash);

        when(
                authSessionRepository.findByRefreshTokenHash(
                        refreshTokenHash
                )
        )
                .thenReturn(Optional.of(authSession));

        when(authSession.isRevoked())
                .thenReturn(true);

        // when
        logoutService.logout(refreshToken);

        // then
        verify(
                authSession,
                never()
        ).revoke(any());
    }

    @Test
    @DisplayName("Refresh Token이 없어도 로그아웃은 정상 종료된다")
    void logoutDoesNothingWhenTokenIsMissing() {
        // when
        logoutService.logout(null);

        // then
        verify(
                tokenHashService,
                never()
        ).hash(any());

        verify(
                authSessionRepository,
                never()
        ).findByRefreshTokenHash(any());
    }

    @Test
    @DisplayName("DB에 세션이 없어도 로그아웃은 정상 종료된다")
    void logoutDoesNothingWhenSessionDoesNotExist() {
        // given
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";

        when(tokenHashService.hash(refreshToken))
                .thenReturn(refreshTokenHash);

        when(
                authSessionRepository.findByRefreshTokenHash(
                        refreshTokenHash
                )
        )
                .thenReturn(Optional.empty());

        // when
        logoutService.logout(refreshToken);

        // then
        verify(
                authSession,
                never()
        ).revoke(any());
    }
}