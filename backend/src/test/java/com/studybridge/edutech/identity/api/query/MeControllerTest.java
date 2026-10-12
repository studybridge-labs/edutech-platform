package com.studybridge.edutech.identity.api.query;

import com.studybridge.edutech.global.exception.GlobalExceptionHandler;
import com.studybridge.edutech.global.security.AuthUser;
import com.studybridge.edutech.global.security.SecurityConfig;
import com.studybridge.edutech.global.security.jwt.JwtTokenProvider;
import com.studybridge.edutech.identity.api.query.dto.MeResponse;
import com.studybridge.edutech.identity.application.exception.UserNotFoundException;
import com.studybridge.edutech.identity.application.query.MeQueryService;
import com.studybridge.edutech.identity.domain.Role;
import com.studybridge.edutech.student.domain.Grade;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 내 정보 조회 API(GET /api/v1/me)의 HTTP 요청과 응답을 검증합니다.
 *
 * <p>JWT 필터 → Security 규칙 → Controller → 예외 처리까지 실제 흐름대로 통과시키고,
 * Service와 JwtTokenProvider만 Mock으로 대체합니다.</p>
 */
@WebMvcTest(MeController.class) // MeController 하나만 올리는 가벼운 Spring 테스트
@Import({
        SecurityConfig.class,          // 우리가 만든 Security 규칙(JWT 필터, 401/403 JSON)을 적용
        GlobalExceptionHandler.class   // UserNotFoundException → 404 변환을 적용
})
class MeControllerTest {

    private static final String ME_URL = "/api/v1/me";
    private static final String USER_TOKEN = "user-token";

    // 토큰 안에 들어있다고 가정할 사용자 ID
    private static final UUID USER_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc; // 서버를 띄우지 않고 HTTP 요청을 흉내 내는 도구

    // Controller가 의존하는 Service를 가짜로 교체
    @MockitoBean
    private MeQueryService meQueryService;

    // SecurityConfig가 의존하는 토큰 검증기를 가짜로 교체
    // (진짜를 쓰면 JWT_SECRET 환경변수와 진짜 서명된 토큰이 필요해진다)
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // "user-token"이라는 문자열이 오면, USER_ID를 가진 일반 사용자로 인증된다고 약속
        when(jwtTokenProvider.parseAccessToken(USER_TOKEN))
                .thenReturn(new AuthUser(USER_ID, Role.USER));
    }

    @Test
    @DisplayName("토큰 없이 요청하면 401과 공통 에러 응답을 반환한다")
    void getMe_noToken_returns401() throws Exception {
        // when & then
        mockMvc.perform(get(ME_URL)) // Authorization 헤더 없음
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("ACCESS_TOKEN_INVALID"));

        // Security 단계에서 막혔으므로 Service는 한 번도 호출되면 안 된다
        verify(meQueryService, never()).getMe(any());
    }

    @Test
    @DisplayName("유효한 토큰으로 요청하면 200과 내 정보를 반환한다")
    void getMe_validToken_returns200() throws Exception {
        // given: Service가 돌려줄 응답을 미리 정해둔다
        MeResponse response = new MeResponse(
                USER_ID,
                "student01@example.com",
                "학생01",
                Grade.MIDDLE_2,
                Role.USER,
                List.of()
        );

        when(meQueryService.getMe(USER_ID))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        get(ME_URL)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // API 명세서의 응답 필드 6개가 정확한 이름과 형식으로 나가는지 확인
                .andExpect(jsonPath("$.id").value(USER_ID.toString())) // UUID는 문자열로 나간다
                .andExpect(jsonPath("$.email").value("student01@example.com"))
                .andExpect(jsonPath("$.nickname").value("학생01"))
                .andExpect(jsonPath("$.grade").value("MIDDLE_2"))     // enum은 이름 그대로
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.linkedAccounts").isArray())   // null이 아니라 []
                .andExpect(jsonPath("$.linkedAccounts").isEmpty());

        // 핵심: Controller가 "토큰에서 꺼낸 userId"로 Service를 호출했는지 확인 (IDOR 방지)
        verify(meQueryService).getMe(USER_ID);
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 404 USER_NOT_FOUND를 반환한다")
    void getMe_userNotFound_returns404() throws Exception {
        // given: 토큰은 유효하지만 계정이 DB에 없는 상황
        when(meQueryService.getMe(USER_ID))
                .thenThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(
                        get(ME_URL)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.traceId").exists()); // 추적용 ID가 함께 나가는지
    }
}