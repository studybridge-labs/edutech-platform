package com.studybridge.edutech.identity.api.command;

import com.studybridge.edutech.global.security.jwt.JwtProperties;
import com.studybridge.edutech.identity.api.command.dto.LoginRequest;
import com.studybridge.edutech.identity.api.command.dto.LoginResponse;
import com.studybridge.edutech.identity.application.command.LoginResult;
import com.studybridge.edutech.identity.application.command.LoginService;
import com.studybridge.edutech.identity.application.command.RefreshService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.studybridge.edutech.identity.application.command.LogoutService;

/**
 * LOCAL 로그인 API를 제공합니다.
 *
 * <p>Access Token은 Response Body로 반환하고,
 * Refresh Token은 JavaScript에서 접근할 수 없는
 * HttpOnly Cookie로 전달합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private static final String REFRESH_TOKEN_COOKIE_NAME =
            "refreshToken";

    private final LoginService loginService;
    private final JwtProperties jwtProperties;
    private final RefreshService refreshService;
    private final LogoutService logoutService;

    public LoginController(
            LoginService loginService,
            JwtProperties jwtProperties,
            RefreshService refreshService,
            LogoutService logoutService
    ) {
        this.loginService = loginService;
        this.jwtProperties = jwtProperties;
        this.refreshService = refreshService;
        this.logoutService = logoutService;
    }

    /**
     * 이메일과 비밀번호로 로그인합니다.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResult result =
                loginService.login(request);

        ResponseCookie refreshTokenCookie =
                createRefreshTokenCookie(
                        result.refreshToken()
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookie.toString()
                )
                .body(result.response());
    }

    /**
     * Refresh Token을 HttpOnly Cookie로 생성합니다.
     */
    private ResponseCookie createRefreshTokenCookie(
            String refreshToken
    ) {
        return ResponseCookie
                .from(
                        REFRESH_TOKEN_COOKIE_NAME,
                        refreshToken
                )
                .httpOnly(true)

                /*
                 * 현재 로컬 개발환경은 HTTP이므로 false입니다.
                 *
                 * 운영환경 HTTPS에서는 반드시 true로 변경합니다.
                 */
                .secure(false)

                /*
                 * Refresh 관련 API에서만 Cookie가 전달되도록
                 * 인증 API 경로로 제한합니다.
                 */
                .path("/api/v1/auth")

                /*
                 * 일반적인 Cross-Site 요청에서 Cookie 전송을 제한합니다.
                 */
                .sameSite("Lax")

                .maxAge(
                        jwtProperties.refreshTokenExpiration()
                )

                .build();
    }

    /**
     * HttpOnly Cookie의 Refresh Token을 이용하여
     * Access Token과 Refresh Token을 재발급합니다.
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(
                    name = REFRESH_TOKEN_COOKIE_NAME,
                    required = false
            )
            String refreshToken
    ) {
        LoginResult result =
                refreshService.refresh(refreshToken);

        ResponseCookie refreshTokenCookie =
                createRefreshTokenCookie(
                        result.refreshToken()
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookie.toString()
                )
                .body(result.response());
    }



    /**
     * 현재 Refresh Token과 연결된 인증 세션을 종료합니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = REFRESH_TOKEN_COOKIE_NAME,
                    required = false
            )
            String refreshToken
    ) {
        logoutService.logout(refreshToken);

        ResponseCookie deletedCookie =
                ResponseCookie
                        .from(
                                REFRESH_TOKEN_COOKIE_NAME,
                                ""
                        )
                        .httpOnly(true)
                        .secure(false)
                        .path("/api/v1/auth")
                        .sameSite("Lax")
                        .maxAge(0)
                        .build();

        return ResponseEntity
                .noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        deletedCookie.toString()
                )
                .build();
    }
}