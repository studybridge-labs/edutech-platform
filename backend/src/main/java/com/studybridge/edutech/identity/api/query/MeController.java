package com.studybridge.edutech.identity.api.query;

import com.studybridge.edutech.global.security.AuthUser;
import com.studybridge.edutech.identity.api.query.dto.MeResponse;
import com.studybridge.edutech.identity.application.query.MeQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인한 사용자 본인의 정보를 조회하는 API를 제공합니다. (QUERY)
 *
 * <p>사용자 ID를 URL이나 요청 값으로 받지 않고,
 * JWT 인증 필터가 SecurityContext에 저장한 AuthUser에서 꺼내 사용합니다.
 * 따라서 다른 사용자의 정보를 조회할 방법이 없습니다. (security.md 22. IDOR 방어, 23. /me API)</p>
 */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final MeQueryService meQueryService;

    public MeController(MeQueryService meQueryService) {
        this.meQueryService = meQueryService;
    }

    /**
     * 내 정보를 조회합니다.
     *
     * <p>이 API는 SecurityConfig의 anyRequest().authenticated() 규칙에 해당하므로
     * 인증되지 않은 요청은 Controller에 도달하기 전에 401로 차단됩니다.
     * 그래서 여기서 authUser는 항상 존재합니다.</p>
     *
     * @param authUser 현재 요청의 사용자 (JWT 인증 필터가 저장한 값)
     * @return 내 정보
     */
    @GetMapping
    public ResponseEntity<MeResponse> getMe(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        MeResponse response = meQueryService.getMe(authUser.userId());

        return ResponseEntity.ok(response);
    }
}