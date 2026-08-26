package com.studybridge.edutech.identity.application.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Refresh Token 생성 로직을 검증합니다.
 */
class RefreshTokenGeneratorTest {

    private final RefreshTokenGenerator generator =
            new RefreshTokenGenerator();

    @Test
    @DisplayName("Refresh Token은 매번 새로운 랜덤 값을 생성한다")
    void generatesDifferentRefreshTokens() {
        // when
        String firstToken = generator.generate();
        String secondToken = generator.generate();

        // then
        assertThat(firstToken)
                .isNotEqualTo(secondToken);
    }

    @Test
    @DisplayName("Refresh Token은 256-bit 랜덤 값을 Base64 URL 형식으로 생성한다")
    void generatesBase64UrlToken() {
        // when
        String token = generator.generate();

        // then
        assertThat(token)
                .hasSize(43);

        assertThat(token)
                .matches("[A-Za-z0-9_-]+");
    }
}