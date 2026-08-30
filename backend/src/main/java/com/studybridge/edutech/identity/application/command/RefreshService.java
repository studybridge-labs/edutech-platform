package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.global.security.jwt.JwtProperties;
import com.studybridge.edutech.global.security.jwt.JwtTokenProvider;
import com.studybridge.edutech.identity.api.command.dto.LoginResponse;
import com.studybridge.edutech.identity.application.exception.AccountNotActiveException;
import com.studybridge.edutech.identity.application.exception.InvalidRefreshTokenException;
import com.studybridge.edutech.identity.application.token.RefreshTokenGenerator;
import com.studybridge.edutech.identity.application.token.TokenHashService;
import com.studybridge.edutech.identity.domain.AuthSession;
import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.identity.domain.UserStatus;
import com.studybridge.edutech.identity.infrastructure.AuthSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Refresh Token을 이용하여 인증 정보를 재발급합니다.
 *
 * <p>Refresh Token Rotation을 적용하여 기존 Token을 폐기하고
 * 새로운 Refresh Token과 Access Token을 발급합니다.</p>
 */
@Service
public class RefreshService {

    private final AuthSessionRepository authSessionRepository;
    private final TokenHashService tokenHashService;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public RefreshService(
            AuthSessionRepository authSessionRepository,
            TokenHashService tokenHashService,
            RefreshTokenGenerator refreshTokenGenerator,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties
    ) {
        this.authSessionRepository = authSessionRepository;
        this.tokenHashService = tokenHashService;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public LoginResult refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        String refreshTokenHash =
                tokenHashService.hash(refreshToken);

        AuthSession currentSession =
                authSessionRepository
                        .findByRefreshTokenHash(refreshTokenHash)
                        .orElseThrow(InvalidRefreshTokenException::new);

        Instant now = Instant.now();

        if (currentSession.isRevoked()
                || currentSession.isExpired(now)) {
            throw new InvalidRefreshTokenException();
        }

        User user = currentSession.getUser();

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }

        /*
         * 기존 Refresh Token을 즉시 폐기합니다.
         * 같은 Token을 다시 사용할 수 없도록 Rotation 합니다.
         */
        currentSession.revoke(now);

        String newAccessToken =
                jwtTokenProvider.generateAccessToken(user);

        String newRefreshToken =
                refreshTokenGenerator.generate();

        String newRefreshTokenHash =
                tokenHashService.hash(newRefreshToken);

        AuthSession newSession = AuthSession.create(
                user,
                newRefreshTokenHash,
                now.plus(jwtProperties.refreshTokenExpiration())
        );

        authSessionRepository.save(newSession);

        LoginResponse response = LoginResponse.of(
                newAccessToken,
                jwtProperties.accessTokenExpiration().toSeconds()
        );

        return new LoginResult(
                response,
                newRefreshToken
        );
    }
}