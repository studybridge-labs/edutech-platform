package com.studybridge.edutech.identity.api.command;

import com.studybridge.edutech.global.exception.GlobalExceptionHandler;
import com.studybridge.edutech.global.security.SecurityConfig;
import com.studybridge.edutech.identity.api.command.dto.SignupRequest;
import com.studybridge.edutech.identity.api.command.dto.SignupResponse;
import com.studybridge.edutech.identity.application.command.SignupService;
import com.studybridge.edutech.identity.application.exception.EmailAlreadyExistsException;
import com.studybridge.edutech.student.domain.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LOCAL 회원가입 API의 HTTP 요청 및 응답을 검증합니다.
 *
 * <p>Controller 계층을 대상으로 정상 응답,
 * Validation 실패, 비즈니스 예외 응답을 확인합니다.</p>
 */
@WebMvcTest(SignupController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SignupService signupService;

    @Test
    @DisplayName("정상적인 회원가입 요청은 201 Created를 반환한다")
    void signupSuccess() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "student01@example.com",
                "Password123!",
                "학생01",
                Grade.MIDDLE_2
        );

        UUID userId = UUID.randomUUID();

        SignupResponse response = new SignupResponse(
                userId,
                "student01@example.com",
                "학생01",
                Grade.MIDDLE_2
        );

        when(signupService.signup(any(SignupRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("student01@example.com"))
                .andExpect(jsonPath("$.nickname").value("학생01"))
                .andExpect(jsonPath("$.grade").value("MIDDLE_2"));
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400 Bad Request를 반환한다")
    void signupFailsWhenEmailIsInvalid() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "wrong-email",
                "Password123!",
                "학생01",
                Grade.MIDDLE_2
        );

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("올바른 이메일 형식이 아닙니다."))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 409 Conflict를 반환한다")
    void signupFailsWhenEmailAlreadyExists() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "student01@example.com",
                "Password123!",
                "학생01",
                Grade.MIDDLE_2
        );

        when(signupService.signup(any(SignupRequest.class)))
                .thenThrow(new EmailAlreadyExistsException());

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code")
                        .value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message")
                        .value("이미 사용 중인 이메일입니다."))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }
}