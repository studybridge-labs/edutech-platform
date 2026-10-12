package com.studybridge.edutech.identity.api.command;

import com.studybridge.edutech.global.exception.GlobalExceptionHandler;
import com.studybridge.edutech.global.security.AuthUser;
import com.studybridge.edutech.global.security.SecurityConfig;
import com.studybridge.edutech.global.security.jwt.JwtTokenProvider;
import com.studybridge.edutech.identity.api.command.dto.UpdateProfileRequest;
import com.studybridge.edutech.identity.api.query.dto.MeResponse;
import com.studybridge.edutech.identity.application.command.ProfileService;
import com.studybridge.edutech.identity.application.exception.StudentProfileNotFoundException;
import com.studybridge.edutech.identity.domain.Role;
import com.studybridge.edutech.student.domain.Grade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 프로필 변경 API(PATCH /api/v1/me/profile)의 HTTP 요청과 응답을 검증합니다.
 *
 * <p>JWT 필터 → Security 규칙 → @Valid 검증 → Controller → 예외 처리까지 실제 흐름대로 통과시키고,
 * Service와 JwtTokenProvider만 Mock으로 대체합니다.</p>
 */
@WebMvcTest(ProfileController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class ProfileControllerTest {

    private static final String PROFILE_URL = "/api/v1/me/profile";
    private static final String USER_TOKEN = "user-token";
    private static final UUID USER_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // "user-token"이 오면 USER_ID를 가진 일반 사용자로 인증된다고 약속
        when(jwtTokenProvider.parseAccessToken(USER_TOKEN))
                .thenReturn(new AuthUser(USER_ID, Role.USER));
    }

    @Test
    @DisplayName("토큰 없이 요청하면 401을 반환하고 Service는 호출되지 않는다")
    void updateProfile_noToken_returns401() throws Exception {
        // given
        String body = """
                {
                  "nickname": "새닉네임"
                }
                """;

        // when & then
        mockMvc.perform(
                        patch(PROFILE_URL) // Authorization 헤더 없음
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCESS_TOKEN_INVALID"));

        verify(profileService, never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("유효한 토큰과 올바른 요청이면 200과 변경된 내 정보를 반환한다")
    void updateProfile_validRequest_returns200() throws Exception {
        // given: 프론트가 실제로 보낼 JSON
        String body = """
                {
                  "nickname": "새닉네임",
                  "grade": "MIDDLE_3"
                }
                """;

        // 위 JSON이 변환되었을 때 기대하는 DTO
        UpdateProfileRequest expectedRequest =
                new UpdateProfileRequest("새닉네임", Grade.MIDDLE_3);

        MeResponse response = new MeResponse(
                USER_ID,
                "student01@example.com",
                "새닉네임",
                Grade.MIDDLE_3,
                Role.USER,
                List.of()
        );

        when(profileService.updateProfile(USER_ID, expectedRequest))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        patch(PROFILE_URL)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.grade").value("MIDDLE_3"));

        // 핵심: 토큰의 userId + JSON에서 변환된 DTO가 정확히 Service에 전달되었는지 확인
        verify(profileService).updateProfile(USER_ID, expectedRequest);
    }

    @Test
    @DisplayName("닉네임이 2자 미만이면 400을 반환하고 Service는 호출되지 않는다")
    void updateProfile_nicknameTooShort_returns400() throws Exception {
        // given: "가"는 공백이 아니므로 @Pattern은 통과, @Size만 실패한다
        String body = """
                {
                  "nickname": "가"
                }
                """;

        // when & then
        mockMvc.perform(
                        patch(PROFILE_URL)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("닉네임은 2자 이상 50자 이하로 입력해주세요."));

        // @Valid에서 막혔으므로 Service까지 가면 안 된다
        verify(profileService, never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("닉네임이 공백으로만 이루어져 있으면 400을 반환하고 Service는 호출되지 않는다")
    void updateProfile_nicknameBlank_returns400() throws Exception {
        // given: 공백 3칸은 길이 조건(@Size)은 통과, @Pattern만 실패한다
        String body = """
                {
                  "nickname": "   "
                }
                """;

        // when & then
        mockMvc.perform(
                        patch(PROFILE_URL)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("닉네임은 공백만으로 입력할 수 없습니다."));

        verify(profileService, never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("학생 프로필이 없는 계정이 학년을 변경하면 404 STUDENT_PROFILE_NOT_FOUND를 반환한다")
    void updateProfile_studentProfileNotFound_returns404() throws Exception {
        // given: nickname 필드를 아예 보내지 않는다 → DTO에서는 null (변경 안 함)
        String body = """
                {
                  "grade": "MIDDLE_3"
                }
                """;

        UpdateProfileRequest expectedRequest =
                new UpdateProfileRequest(null, Grade.MIDDLE_3);

        when(profileService.updateProfile(USER_ID, expectedRequest))
                .thenThrow(new StudentProfileNotFoundException());

        // when & then
        mockMvc.perform(
                        patch(PROFILE_URL)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("STUDENT_PROFILE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("학생 프로필을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.traceId").exists());
    }
}