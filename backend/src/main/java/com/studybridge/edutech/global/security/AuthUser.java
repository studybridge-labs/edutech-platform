package com.studybridge.edutech.global.security;

import com.studybridge.edutech.identity.domain.Role;

import java.util.UUID;

/**
 * Access Token 검증을 통과한 "현재 요청의 사용자" 정보입니다.
 *
 * <p>JWT 필터가 이 객체를 SecurityContext에 저장하고,
 * Controller는 @AuthenticationPrincipal 로 꺼내서 사용합니다.</p>
 *
 * <p>왜 User 엔티티가 아니라 별도 record인가?</p>
 * <ul>
 *   <li>토큰에 들어 있는 정보(userId, role)만으로 만들 수 있다.
 *       → 요청마다 DB를 조회하지 않는다. (Stateless)</li>
 *   <li>Entity를 Security 영역에 들고 다니면 지연 로딩, 영속성 문제 등이 따라온다.</li>
 *   <li>email, nickname처럼 바뀔 수 있는 값은 넣지 않는다.
 *       필요하면 userId로 DB에서 조회한다. (/me API에서 그렇게 할 예정)</li>
 * </ul>
 *
 * @param userId 사용자 ID (JWT의 subject)
 * @param role   사용자 권한 (JWT의 role claim)
 */
public record AuthUser(
        UUID userId,
        Role role
) {
}