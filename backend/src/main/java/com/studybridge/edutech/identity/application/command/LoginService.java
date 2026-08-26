package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.global.security.jwt.JwtProperties;
import com.studybridge.edutech.global.security.jwt.JwtTokenProvider;
import com.studybridge.edutech.identity.api.command.dto.LoginRequest;
import com.studybridge.edutech.identity.api.command.dto.LoginResponse;
import com.studybridge.edutech.identity.application.exception.AccountNotActiveException;
import com.studybridge.edutech.identity.application.exception.InvalidCredentialsException;
import com.studybridge.edutech.identity.application.token.RefreshTokenGenerator;
import com.studybridge.edutech.identity.application.token.TokenHashService;
import com.studybridge.edutech.identity.domain.AuthSession;
import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.identity.domain.UserStatus;
import com.studybridge.edutech.identity.infrastructure.AuthSessionRepository;
import com.studybridge.edutech.identity.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * LOCAL 로그인을 처리합니다.
 *
 * <p>이메일/비밀번호 인증에 성공하면 Access Token과
 * Refresh Token을 생성하고 로그인 Session을 저장합니다.</p>
 */
@Service
public class LoginService {

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final TokenHashService tokenHashService;
    private final JwtProperties jwtProperties;

    public LoginService(
            UserRepository userRepository,
            AuthSessionRepository authSessionRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenGenerator refreshTokenGenerator,
            TokenHashService tokenHashService,
            JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.tokenHashService = tokenHashService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 이메일과 비밀번호로 사용자를 인증합니다.
     */
    @Transactional
    public LoginResult login(LoginRequest request) {

        User user = userRepository
                .findByEmailIgnoreCase(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        validatePassword(
                request.password(),
                user.getPasswordHash()
        );

        validateAccountStatus(user);

        String accessToken =
                jwtTokenProvider.generateAccessToken(user);

        String refreshToken =
                refreshTokenGenerator.generate();

        String refreshTokenHash =
                tokenHashService.hash(refreshToken);

        Instant refreshTokenExpiresAt =
                Instant.now().plus(
                        jwtProperties.refreshTokenExpiration()
                );

        AuthSession authSession = AuthSession.create(
                user,
                refreshTokenHash,
                refreshTokenExpiresAt
        );

        authSessionRepository.save(authSession);

        long expiresIn =
                jwtProperties
                        .accessTokenExpiration()
                        .toSeconds();

        LoginResponse response =
                LoginResponse.of(
                        accessToken,
                        expiresIn
                );

        return new LoginResult(
                response,
                refreshToken
        );
    }

    /**
     * 입력한 비밀번호와 DB의 BCrypt Hash를 비교합니다.
     */
    private void validatePassword(
            String rawPassword,
            String passwordHash
    ) {
        if (passwordHash == null
                || !passwordEncoder.matches(
                rawPassword,
                passwordHash
        )) {

            throw new InvalidCredentialsException();
        }
    }

    /**
     * 정상 ACTIVE 계정만 로그인할 수 있도록 제한합니다.
     */
    private void validateAccountStatus(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }
    }
}