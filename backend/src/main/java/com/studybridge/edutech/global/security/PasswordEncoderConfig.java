package com.studybridge.edutech.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 사용자 비밀번호 암호화를 위한 설정입니다.
 *
 * <p>LOCAL 회원가입 시 평문 비밀번호를 BCrypt Hash로 변환하고,
 * 이후 로그인 시 비밀번호 일치 여부를 검증하는 데 사용합니다.</p>
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 비밀번호 암호화에 사용할 PasswordEncoder를 등록합니다.
     *
     * @return BCrypt 기반 PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}