package com.studybridge.edutech.identity.api.query.dto;

import com.studybridge.edutech.identity.domain.Role;
import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.student.domain.Grade;

import java.util.List;
import java.util.UUID;

/**
 * 로그인한 사용자 본인의 정보를 반환하는 DTO입니다. (GET /api/v1/me)
 *
 * <p>비밀번호 Hash, 계정 상태 같은 내부 정보는 응답에 포함하지 않습니다.
 * (보안 문서: Entity 직접 Response 금지)</p>
 *
 * @param id             사용자 ID
 * @param email          이메일
 * @param nickname       닉네임
 * @param grade          학년. StudentProfile이 없는 사용자(ADMIN)는 null
 * @param role           권한 (USER / ADMIN)
 * @param linkedAccounts 연결된 소셜 계정 목록 (예: ["GOOGLE"])
 */
public record MeResponse(
        UUID id,
        String email,
        String nickname,
        Grade grade,
        Role role,
        List<String> linkedAccounts
) {

    /**
     * User와 학년 정보를 내 정보 응답으로 변환합니다.
     *
     * <p>소셜 로그인(SocialAccount)은 아직 구현 전이므로
     * linkedAccounts는 빈 목록으로 반환합니다.
     * 소셜 계정 연결 기능을 만들 때 이 부분을 채웁니다.</p>
     *
     * @param user  사용자
     * @param grade 학년. StudentProfile이 없으면 null
     * @return 내 정보 응답
     */
    public static MeResponse of(User user, Grade grade) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                grade,
                user.getRole(),
                List.of()
        );
    }
}