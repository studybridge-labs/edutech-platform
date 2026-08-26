package com.studybridge.edutech.identity.application.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Refresh Token Hash 생성 로직을 검증합니다.
 */
class TokenHashServiceTest {

    private final TokenHashService tokenHashService =
            new TokenHashService();

    @Test
    @DisplayName("같은 Refresh Token은 항상 같은 SHA-256 Hash를 생성한다")
    void sameTokenCreatesSameHash() {
        // given
        String token = "refresh-token";

        // when
        String firstHash = tokenHashService.hash(token);
        String secondHash = tokenHashService.hash(token);

        // then
        assertThat(firstHash)
                .isEqualTo(secondHash);

        assertThat(firstHash)
                .hasSize(64);

        assertThat(firstHash)
                .isEqualTo(
                        "0eb17643d4e9261163783a420859c92c7d212fa9624106a12b510afbec266120"
                );
    }

    @Test
    @DisplayName("다른 Refresh Token은 다른 Hash를 생성한다")
    void differentTokenCreatesDifferentHash() {
        // when
        String firstHash =
                tokenHashService.hash("refresh-token-1");

        String secondHash =
                tokenHashService.hash("refresh-token-2");

        // then
        assertThat(firstHash)
                .isNotEqualTo(secondHash);
    }
}