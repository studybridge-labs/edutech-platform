package com.studybridge.edutech.global.security.jwt;

import com.studybridge.edutech.global.security.AuthUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 요청의 Authorization 헤더에서 Access Token을 꺼내 검증하고,
 * 유효하면 SecurityContext에 인증 정보를 저장합니다.
 *
 * <p>이 필터는 요청을 거절하지 않습니다.</p>
 * <ul>
 *   <li>토큰이 없거나 유효하지 않으면 인증 정보 없이 다음 필터로 넘깁니다.</li>
 *   <li>접근 허용 여부는 SecurityConfig의 authorizeHttpRequests 규칙이 판단합니다.</li>
 *   <li>그래서 permitAll 경로(로그인 등)는 잘못된 토큰이 있어도 정상 동작합니다.</li>
 * </ul>
 *
 * <p>@Component를 붙이지 않는 이유:
 * Spring Boot는 Bean으로 등록된 Filter를 서블릿 필터로도 자동 등록합니다.
 * 그러면 Security 필터 체인과 서블릿 필터, 두 곳에서 실행될 수 있으므로
 * SecurityConfig에서 직접 생성해 Security 필터 체인에만 넣습니다.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * Authorization 헤더의 토큰 접두사입니다.
     * 예: "Authorization: Bearer eyJhbGciOi..."
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Spring Security가 권한을 비교할 때 사용하는 접두사입니다.
     * hasRole("ADMIN") 은 내부적으로 "ROLE_ADMIN" 권한을 찾습니다.
     */
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            try {
                AuthUser authUser = jwtTokenProvider.parseAccessToken(token);

                List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority(ROLE_PREFIX + authUser.role().name())
                );

                /*
                 * "인증 완료" 상태의 Authentication 객체를 만든다.
                 * - principal   : AuthUser → Controller에서 @AuthenticationPrincipal 로 꺼냄
                 * - credentials : null     → 비밀번호 같은 자격 증명은 들고 다니지 않음
                 * - authorities : ROLE_USER / ROLE_ADMIN
                 */
                UsernamePasswordAuthenticationToken authentication =
                        UsernamePasswordAuthenticationToken.authenticated(
                                authUser,
                                null,
                                authorities
                        );

                /*
                 * 새 SecurityContext를 만들어 저장한다.
                 * (기존 Context를 꺼내 수정하는 것보다, 새로 만들어 교체하는 방식이 권장됨)
                 */
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

            } catch (InvalidAccessTokenException e) {
                /*
                 * 유효하지 않은 토큰: 인증 정보를 비우고 그대로 진행한다.
                 * 보호된 API라면 이후 authorizeHttpRequests에서 거절된다.
                 * 토큰 원문은 로그에 남기지 않는다. (보안 문서: 민감 정보 로그 금지)
                 */
                SecurityContextHolder.clearContext();
                log.debug("유효하지 않은 Access Token 요청. uri={}", request.getRequestURI());
            }
        }

        // 다음 필터로 요청을 넘긴다. (이 줄이 없으면 요청이 여기서 멈춘다)
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 "Bearer " 를 뗀 토큰 문자열을 꺼냅니다.
     *
     * @return 토큰 문자열. 헤더가 없거나 Bearer 형식이 아니면 null
     */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        return token.isEmpty() ? null : token;
    }
}