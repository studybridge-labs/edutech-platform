package com.studybridge.edutech.global.security;

import com.studybridge.edutech.global.security.jwt.InvalidAccessTokenException;
import com.studybridge.edutech.global.security.jwt.JwtTokenProvider;
import com.studybridge.edutech.identity.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security 설정(JWT 필터 + 401/403 처리 + ADMIN 경로 규칙)이
 * 실제 HTTP 요청에서 의도대로 동작하는지 검증합니다.
 *
 * <p>아직 /me, /admin 같은 실제 보호 API가 없으므로,
 * 이 테스트 안에 테스트 전용 Controller(TestController)를 만들어 사용합니다.</p>
 *
 * <p>JwtTokenProvider는 Mock으로 대체하여,
 * 정해진 문자열("user-token", "admin-token", "invalid-token")에 정해진 결과를 돌려주게 합니다.</p>
 */
@WebMvcTest(controllers = SecurityConfigTest.TestController.class)
@Import({
        SecurityConfig.class,
        SecurityConfigTest.TestController.class
})
class SecurityConfigTest {

    private static final String PROTECTED_URL = "/api/v1/test/protected";
    private static final String ADMIN_URL = "/api/v1/admin/test";
    private static final String PERMIT_ALL_URL = "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        when(jwtTokenProvider.parseAccessToken("user-token"))
                .thenReturn(new AuthUser(UUID.randomUUID(), Role.USER));

        when(jwtTokenProvider.parseAccessToken("admin-token"))
                .thenReturn(new AuthUser(UUID.randomUUID(), Role.ADMIN));

        when(jwtTokenProvider.parseAccessToken("invalid-token"))
                .thenThrow(new InvalidAccessTokenException("유효하지 않은 Access Token입니다."));
    }

    // =====================================================================
    // 401 Unauthorized
    // =====================================================================

    @Test
    @DisplayName("토큰 없이 보호된 API를 요청하면 401과 공통 에러 응답을 반환한다")
    void noToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("ACCESS_TOKEN_INVALID"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 보호된 API를 요청하면 401을 반환한다")
    void invalidToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCESS_TOKEN_INVALID"));
    }

    @Test
    @DisplayName("토큰 없이 ADMIN API를 요청하면 403이 아니라 401을 반환한다")
    void noToken_adminApi_returns401() throws Exception {
        mockMvc.perform(get(ADMIN_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCESS_TOKEN_INVALID"));
    }

    // =====================================================================
    // 403 Forbidden
    // =====================================================================

    @Test
    @DisplayName("USER 권한으로 ADMIN API를 요청하면 403과 공통 에러 응답을 반환한다")
    void userToken_adminApi_returns403() throws Exception {
        mockMvc.perform(get(ADMIN_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    // =====================================================================
    // 정상 통과
    // =====================================================================

    @Test
    @DisplayName("유효한 USER 토큰이면 보호된 API에 접근할 수 있다")
    void userToken_protectedApi_returns200() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN 토큰이면 ADMIN API에 접근할 수 있다")
    void adminToken_adminApi_returns200() throws Exception {
        mockMvc.perform(get(ADMIN_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("permitAll 경로는 유효하지 않은 토큰이 있어도 차단되지 않는다")
    void invalidToken_permitAllApi_returns200() throws Exception {
        mockMvc.perform(post(PERMIT_ALL_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isOk());
    }

    /**
     * 테스트 전용 Controller.
     *
     * <p>테스트 클래스 안에 있는 클래스는 일반 컴포넌트 스캔에서 제외되므로
     * 실제 애플리케이션에는 등록되지 않고, 위의 @Import로 이 테스트에서만 등록됩니다.</p>
     */
    @RestController
    public static class TestController {

        @GetMapping(PROTECTED_URL)
        public String protectedApi() {
            return "ok";
        }

        @GetMapping(ADMIN_URL)
        public String adminApi() {
            return "admin ok";
        }

        /**
         * permitAll 경로 동작 확인용.
         * 실제 LoginController는 이 테스트에 로딩되지 않으므로 같은 경로를 대신 만든다.
         */
        @PostMapping(PERMIT_ALL_URL)
        public String permitAllApi() {
            return "login ok";
        }
    }
}