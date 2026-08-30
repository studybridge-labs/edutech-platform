package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.global.security.jwt.JwtProperties;
import com.studybridge.edutech.global.security.jwt.JwtTokenProvider;
import com.studybridge.edutech.identity.application.exception.AccountNotActiveException;
import com.studybridge.edutech.identity.application.exception.InvalidRefreshTokenException;
import com.studybridge.edutech.identity.application.token.RefreshTokenGenerator;
import com.studybridge.edutech.identity.application.token.TokenHashService;
import com.studybridge.edutech.identity.domain.AuthSession;
import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.identity.domain.UserStatus;
import com.studybridge.edutech.identity.infrastructure.AuthSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Refresh Token 재발급 서비스 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private TokenHashService tokenHashService;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private User user;

    @Mock
    private AuthSession currentSession;

    private RefreshService refreshService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
                "test-secret",
                Duration.ofMinutes(15),
                Duration.ofDays(14)
        );

        refreshService = new RefreshService(
                authSessionRepository,
                tokenHashService,
                refreshTokenGenerator,
                jwtTokenProvider,
                jwtProperties
        );
    }

    @Test
    @DisplayName("유효한 Refresh Token이면 기존 세션을 폐기하고 새 토큰을 발급한다")
    void refreshSuccess() {
        // given
        String oldRefreshToken = "old-refresh-token";
        String oldRefreshTokenHash = "old-refresh-token-hash";

        String newRefreshToken = "new-refresh-token";
        String newRefreshTokenHash = "new-refresh-token-hash";

        String newAccessToken = "new-access-token";

        when(tokenHashService.hash(oldRefreshToken))
                .thenReturn(oldRefreshTokenHash);

        when(authSessionRepository.findByRefreshTokenHash(oldRefreshTokenHash))
                .thenReturn(Optional.of(currentSession));

        when(currentSession.isRevoked())
                .thenReturn(false);

        when(currentSession.isExpired(any(Instant.class)))
                .thenReturn(false);

        when(currentSession.getUser())
                .thenReturn(user);

        when(user.getStatus())
                .thenReturn(UserStatus.ACTIVE);

        when(jwtTokenProvider.generateAccessToken(user))
                .thenReturn(newAccessToken);

        when(refreshTokenGenerator.generate())
                .thenReturn(newRefreshToken);

        when(tokenHashService.hash(newRefreshToken))
                .thenReturn(newRefreshTokenHash);

        // when
        LoginResult result =
                refreshService.refresh(oldRefreshToken);

        // then
        assertThat(result.response().accessToken())
                .isEqualTo(newAccessToken);

        assertThat(result.response().tokenType())
                .isEqualTo("Bearer");

        assertThat(result.response().expiresIn())
                .isEqualTo(900);

        assertThat(result.refreshToken())
                .isEqualTo(newRefreshToken);

        /*
         * 기존 Refresh Session이 revoke 되었는지 확인합니다.
         */
        verify(currentSession)
                .revoke(any(Instant.class));

        /*
         * Rotation 과정에서 새로운 AuthSession이
         * 저장되었는지 확인합니다.
         */
        verify(authSessionRepository)
                .save(any(AuthSession.class));
    }

    @Test
    @DisplayName("Refresh Token이 없으면 재발급할 수 없다")
    void refreshFailsWhenTokenIsMissing() {
        assertThatThrownBy(
                () -> refreshService.refresh(null)
        )
                .isInstanceOf(
                        InvalidRefreshTokenException.class
                );

        verify(
                authSessionRepository,
                never()
        ).findByRefreshTokenHash(any());
    }

    @Test
    @DisplayName("이미 폐기된 Refresh Session은 재사용할 수 없다")
    void refreshFailsWhenSessionIsRevoked() {
        // given
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";

        when(tokenHashService.hash(refreshToken))
                .thenReturn(refreshTokenHash);

        when(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .thenReturn(Optional.of(currentSession));

        when(currentSession.isRevoked())
                .thenReturn(true);

        // when & then
        assertThatThrownBy(
                () -> refreshService.refresh(refreshToken)
        )
                .isInstanceOf(
                        InvalidRefreshTokenException.class
                );

        verify(
                jwtTokenProvider,
                never()
        ).generateAccessToken(any());
    }

    @Test
    @DisplayName("ACTIVE 상태가 아닌 사용자는 토큰을 재발급할 수 없다")
    void refreshFailsWhenUserIsNotActive() {
        // given
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";

        when(tokenHashService.hash(refreshToken))
                .thenReturn(refreshTokenHash);

        when(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .thenReturn(Optional.of(currentSession));

        when(currentSession.isRevoked())
                .thenReturn(false);

        when(currentSession.isExpired(any(Instant.class)))
                .thenReturn(false);

        when(currentSession.getUser())
                .thenReturn(user);

        when(user.getStatus())
                .thenReturn(UserStatus.SUSPENDED);

        // when & then
        assertThatThrownBy(
                () -> refreshService.refresh(refreshToken)
        )
                .isInstanceOf(
                        AccountNotActiveException.class
                );

        verify(
                currentSession,
                never()
        ).revoke(any());

        verify(
                authSessionRepository,
                never()
        ).save(any());
    }
}