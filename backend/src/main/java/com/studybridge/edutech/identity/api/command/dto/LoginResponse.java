package com.studybridge.edutech.identity.api.command.dto;

/**
 * 로그인 성공 시 Frontend에 반환하는 인증 정보입니다.
 *
 * <p>Refresh Token은 응답 Body에 노출하지 않고
 * 이후 HttpOnly Cookie로 전달합니다.</p>
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {

    public static LoginResponse of(
            String accessToken,
            long expiresIn
    ) {
        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresIn
        );
    }
}