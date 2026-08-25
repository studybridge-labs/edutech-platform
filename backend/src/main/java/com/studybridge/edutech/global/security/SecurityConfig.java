package com.studybridge.edutech.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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
                                "/api/v1/auth/signup"
                        ).permitAll()
                        .anyRequest().authenticated()
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
         * 현재 회원가입에서는 Cookie 인증을 사용하지 않으므로 false입니다.
         *
         * Refresh Token을 HttpOnly Cookie로 구현할 때
         * true로 변경하고 Origin 정책도 함께 검토합니다.
         */
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/**",
                configuration
        );

        return source;
    }
}