package com.studybridge.edutech.identity.api.command;

import com.studybridge.edutech.global.security.AuthUser;
import com.studybridge.edutech.identity.api.command.dto.UpdateProfileRequest;
import com.studybridge.edutech.identity.api.query.dto.MeResponse;
import com.studybridge.edutech.identity.application.command.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인한 사용자의 프로필 변경 API입니다. (COMMAND)
 *
 * <p>같은 /api/v1/me 경로라도, 조회(GET)는 api/query의 MeController,
 * 변경(PATCH)은 api/command의 이 Controller가 담당합니다. (CQRS 패키지 분리)</p>
 *
 * <p>인증은 SecurityConfig의 anyRequest().authenticated() 규칙이 처리하므로
 * 이 Controller까지 왔다면 항상 인증된 사용자입니다.</p>
 */
@RestController
@RequestMapping("/api/v1/me")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * 프로필(닉네임, 학년)을 변경합니다.
     *
     * <p>바꿀 항목만 보내면 됩니다. 보내지 않은 항목은 그대로 유지됩니다.</p>
     *
     * @param authUser JWT 필터가 SecurityContext에 넣어둔 인증 사용자 (토큰에서 추출)
     * @param request  변경할 항목. @Valid로 UpdateProfileRequest의 검증 규칙을 실행한다
     * @return 200 OK + 변경 후의 내 정보
     */
    @PatchMapping("/profile")
    public ResponseEntity<MeResponse> updateProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        // userId는 요청 본문이 아니라 반드시 토큰에서 꺼낸 값을 사용한다 (IDOR 방지)
        MeResponse response = profileService.updateProfile(
                authUser.userId(),
                request
        );

        return ResponseEntity.ok(response);
    }
}