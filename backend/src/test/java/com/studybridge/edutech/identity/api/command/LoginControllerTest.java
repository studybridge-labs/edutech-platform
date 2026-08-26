package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.global.exception.GlobalExceptionHandler;
import com.studybridge.edutech.global.security.SecurityConfig;
import com.studybridge.edutech.global.security.jwt.JwtProperties;
import com.studybridge.edutech.identity.api.command.LoginController;
import com.studybridge.edutech.identity.api.command.dto.LoginRequest;
import com.studybridge.edutech.identity.api.command.dto.LoginResponse;
import com.studybridge.edutech.identity.application.exception.InvalidCredentialsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LOCAL 로그인 API의 HTTP 요청과 응답을 검증합니다.
 */
@WebMvcTest(LoginController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @Test
    @DisplayName("정상 로그인 요청은 Access Token과 Refresh Token Cookie를 반환한다")
    void loginSuccess() throws Exception {
        // given
        LoginRequest request = new LoginRequest(
                "student01@example.com",
                "Password123!"
        );

        LoginResponse response = LoginResponse.of(
                "access-token",
                900
        );

        LoginResult result = new LoginResult(
                response,
                "refresh-token"
        );

        when(loginService.login(any(LoginRequest.class)))
                .thenReturn(result);

        when(jwtProperties.refreshTokenExpiration())
                .thenReturn(Duration.ofDays(14));

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("access-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(900))

                /**
                 * Refresh Token은 JSON Body가 아니라
                 * Set-Cookie Header로 전달되어야 합니다.
                 */
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString(
                                "refreshToken=refresh-token"
                        )
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString(
                                "HttpOnly"
                        )
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString(
                                "SameSite=Lax"
                        )
                ));
    }

    @Test
    @DisplayName("잘못된 로그인 정보이면 401 Unauthorized를 반환한다")
    void loginFailsWhenCredentialsAreInvalid() throws Exception {
        // given
        LoginRequest request = new LoginRequest(
                "student01@example.com",
                "WrongPassword123!"
        );

        when(loginService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message")
                        .value("이메일 또는 비밀번호가 올바르지 않습니다."))
                .andExpect(jsonPath("$.traceId")
                        .isNotEmpty());
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400 Bad Request를 반환한다")
    void loginFailsWhenEmailIsInvalid() throws Exception {
        // given
        LoginRequest request = new LoginRequest(
                "wrong-email",
                "Password123!"
        );

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("올바른 이메일 형식이 아닙니다."))
                .andExpect(jsonPath("$.traceId")
                        .isNotEmpty());
    }
}