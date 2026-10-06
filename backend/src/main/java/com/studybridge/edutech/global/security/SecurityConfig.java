package com.studybridge.edutech.global.security;

import com.studybridge.edutech.global.security.jwt.JwtAuthenticationFilter;   // ★ 추가
import com.studybridge.edutech.global.security.jwt.JwtTokenProvider;          // ★ 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;   // ★ 추가
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 애플리케이션의 HTTP Security 정책을 설정합니다.
 *
 * <p>JWT 기반 Stateless REST API를 기준으로 구성하며,
 * Frontend 애플리케이션에서 Backend API를 호출할 수 있도록
 * CORS 정책을 함께 관리합니다.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * ★ 추가: JWT 인증 필터가 토큰 검증에 사용합니다.
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * ★ 추가: 생성자 주입
     */
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                /**
                 * CorsConfigurationSource에서 정의한
                 * CORS 정책을 Spring Security에 적용합니다.
                 */
                .cors(Customizer.withDefaults())

                /**
                 * JWT 기반 Stateless API 구조이므로
                 * 현재 단계에서는 CSRF를 비활성화합니다.
                 *
                 * Refresh Token Cookie를 구현할 때
                 * CSRF 보호 전략을 다시 검토합니다.
                 */
                .csrf(AbstractHttpConfigurer::disable)

                /**
                 * 서버 Session을 생성하지 않습니다.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                /**
                 * ★ 추가
                 * 매 요청마다 Authorization 헤더의 Access Token을 검증하고,
                 * 유효하면 SecurityContext에 인증 정보를 저장합니다.
                 *
                 * @Component로 등록하지 않고 여기서 직접 생성하여
                 * Security 필터 체인에만 한 번 등록합니다.
                 */
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * Frontend에서 Backend API를 호출할 수 있도록
     * 허용 Origin, HTTP Method, Header를 정의합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        /**
         * 로그인 시 Refresh Token을 HttpOnly Cookie로 전달하므로
         * Credential을 포함한 Cross-Origin 요청을 허용합니다.
         *
         * allowedOrigins는 "*"가 아니라
         * 신뢰할 수 있는 Frontend Origin만 명시해야 합니다.
         */
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/**",
                configuration
        );

        return source;
    }
}