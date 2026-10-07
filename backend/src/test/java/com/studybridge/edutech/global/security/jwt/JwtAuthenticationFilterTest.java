package com.studybridge.edutech.global.security.jwt;

import com.studybridge.edutech.global.security.AuthUser;
import com.studybridge.edutech.identity.domain.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JWT 인증 필터가 Authorization 헤더 상태에 따라
 * SecurityContext를 올바르게 채우거나 비워두는지 검증합니다.
 *
 * <p>Spring을 띄우지 않는 단위 테스트입니다.
 * JwtTokenProvider는 Mock으로 대체하여, 필터 자체의 동작만 확인합니다.
 * (토큰 검증 로직은 JwtTokenProviderTest에서 이미 검증함)</p>
 */
class JwtAuthenticationFilterTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        filter = new JwtAuthenticationFilter(jwtTokenProvider);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        SecurityContextHolder.clearContext();
    }

    /**
     * SecurityContextHolder는 스레드마다 값을 보관(ThreadLocal)한다.
     * 테스트끼리 같은 스레드를 쓸 수 있으므로, 매번 비워서 서로 영향을 주지 않게 한다.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Bearer 토큰이면 SecurityContext에 AuthUser와 ROLE_ 권한이 저장된다")
    void validToken_setsAuthentication() throws Exception {
        // given
        AuthUser authUser = new AuthUser(UUID.randomUUID(), Role.USER);
        when(jwtTokenProvider.parseAccessToken("valid-token")).thenReturn(authUser);

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(authUser);
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");

        // 다음 필터로 요청이 넘어갔는지 확인
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 정보 없이 다음 필터로 넘어간다")
    void noHeader_passesWithoutAuthentication() throws Exception {
        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
        verify(jwtTokenProvider, never()).parseAccessToken(anyString());
    }

    @Test
    @DisplayName("Bearer 형식이 아닌 헤더는 토큰 검증을 시도하지 않는다")
    void nonBearerHeader_isIgnored() throws Exception {
        // given
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
        verify(jwtTokenProvider, never()).parseAccessToken(anyString());
    }

    @Test
    @DisplayName("Bearer 뒤에 토큰이 비어 있으면 토큰 검증을 시도하지 않는다")
    void blankBearerToken_isIgnored() throws Exception {
        // given
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer    ");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
        verify(jwtTokenProvider, never()).parseAccessToken(anyString());
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 인증 정보 없이 다음 필터로 넘어간다 (필터가 직접 거절하지 않음)")
    void invalidToken_passesWithoutAuthentication() throws Exception {
        // given
        when(jwtTokenProvider.parseAccessToken("invalid-token"))
                .thenThrow(new InvalidAccessTokenException("유효하지 않은 Access Token입니다."));

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
    }
}