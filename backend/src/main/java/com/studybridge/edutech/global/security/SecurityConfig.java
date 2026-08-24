package com.studybridge.edutech.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 애플리케이션의 HTTP Security 정책을 설정합니다.
 *
 * <p>현재 API는 JWT 기반 Stateless 인증 구조를 목표로 하므로
 * 서버 Session을 생성하지 않습니다.</p>
 *
 * <p>회원가입과 로그인 등 인증 이전에 필요한 API는
 * 인증 없이 접근할 수 있도록 허용합니다.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * HTTP 요청에 대한 인증 및 보안 정책을 구성합니다.
     *
     * @param http Spring Security HTTP 설정 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception Security 설정 과정에서 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                /**
                 * 현재는 Access Token을 Authorization Header로 전달하는
                 * Stateless REST API 구조이므로 CSRF를 비활성화합니다.
                 *
                 * Refresh Token을 HttpOnly Cookie로 구현하는 단계에서는
                 * Refresh API의 CSRF 방어 전략을 다시 구성합니다.
                 */
                .csrf(AbstractHttpConfigurer::disable)

                /**
                 * 서버 HTTP Session을 사용하지 않습니다.
                 *
                 * JWT 인증에서는 요청마다 Token을 검증하므로
                 * JSESSIONID 기반 Session을 유지할 필요가 없습니다.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/signup"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}