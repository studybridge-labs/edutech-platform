package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.global.security.jwt.JwtProperties;
import com.studybridge.edutech.global.security.jwt.JwtTokenProvider;
import com.studybridge.edutech.identity.api.command.dto.LoginRequest;
import com.studybridge.edutech.identity.application.exception.InvalidCredentialsException;
import com.studybridge.edutech.identity.application.token.RefreshTokenGenerator;
import com.studybridge.edutech.identity.application.token.TokenHashService;
import com.studybridge.edutech.identity.domain.AuthSession;
import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.identity.infrastructure.AuthSessionRepository;
import com.studybridge.edutech.identity.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LOCAL 로그인 Use Case의 비즈니스 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private TokenHashService tokenHashService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private LoginService loginService;

    @Test
    @DisplayName("이메일과 비밀번호가 올바르면 Access Token과 Refresh Session을 생성한다")
    void loginSuccess() {
        // given
        LoginRequest request = new LoginRequest(
                "student01@example.com",
                "Password123!"
        );

        User user = User.createLocal(
                "student01@example.com",
                "encoded-password",
                "학생01"
        );

        when(userRepository.findByEmailIgnoreCase(request.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )).thenReturn(true);

        when(jwtTokenProvider.generateAccessToken(user))
                .thenReturn("access-token");

        when(refreshTokenGenerator.generate())
                .thenReturn("refresh-token");

        when(tokenHashService.hash("refresh-token"))
                .thenReturn("refresh-token-hash");

        when(jwtProperties.refreshTokenExpiration())
                .thenReturn(Duration.ofDays(14));

        when(jwtProperties.accessTokenExpiration())
                .thenReturn(Duration.ofMinutes(15));

        when(authSessionRepository.save(any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        LoginResult result = loginService.login(request);

        // then
        assertThat(result.response().accessToken())
                .isEqualTo("access-token");

        assertThat(result.response().tokenType())
                .isEqualTo("Bearer");

        assertThat(result.response().expiresIn())
                .isEqualTo(900);

        assertThat(result.refreshToken())
                .isEqualTo("refresh-token");

        ArgumentCaptor<AuthSession> sessionCaptor =
                ArgumentCaptor.forClass(AuthSession.class);

        verify(authSessionRepository)
                .save(sessionCaptor.capture());

        AuthSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getUser())
                .isSameAs(user);

        assertThat(savedSession.getRefreshTokenHash())
                .isEqualTo("refresh-token-hash");

        assertThat(savedSession.getRevokedAt())
                .isNull();

        verify(passwordEncoder)
                .matches(
                        "Password123!",
                        "encoded-password"
                );

        verify(jwtTokenProvider)
                .generateAccessToken(user);

        verify(refreshTokenGenerator)
                .generate();

        verify(tokenHashService)
                .hash("refresh-token");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 로그인을 거부한다")
    void loginFailsWhenEmailDoesNotExist() {
        // given
        LoginRequest request = new LoginRequest(
                "unknown@example.com",
                "Password123!"
        );

        when(userRepository.findByEmailIgnoreCase(request.email()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtTokenProvider, never())
                .generateAccessToken(any(User.class));

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    @Test
    @DisplayName("비밀번호가 올바르지 않으면 로그인을 거부한다")
    void loginFailsWhenPasswordIsInvalid() {
        // given
        LoginRequest request = new LoginRequest(
                "student01@example.com",
                "WrongPassword123!"
        );

        User user = User.createLocal(
                "student01@example.com",
                "encoded-password",
                "학생01"
        );

        when(userRepository.findByEmailIgnoreCase(request.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        verify(jwtTokenProvider, never())
                .generateAccessToken(any(User.class));

        verify(refreshTokenGenerator, never())
                .generate();

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }
}