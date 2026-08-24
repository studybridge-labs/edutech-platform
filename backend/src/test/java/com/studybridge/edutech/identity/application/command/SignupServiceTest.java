package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.identity.api.command.dto.SignupRequest;
import com.studybridge.edutech.identity.api.command.dto.SignupResponse;
import com.studybridge.edutech.identity.application.exception.EmailAlreadyExistsException;
import com.studybridge.edutech.identity.domain.Role;
import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.identity.domain.UserStatus;
import com.studybridge.edutech.identity.infrastructure.UserRepository;
import com.studybridge.edutech.student.domain.Grade;
import com.studybridge.edutech.student.domain.StudentProfile;
import com.studybridge.edutech.student.infrastructure.StudentProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LOCAL 회원가입 Use Case의 비즈니스 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupService signupService;

    @Test
    @DisplayName("LOCAL 회원가입에 성공하면 사용자와 학생 프로필을 저장한다")
    void signupSuccess() {
        // given
        SignupRequest request = new SignupRequest(
                "Student01@Example.com",
                "Password123!",
                "학생01",
                Grade.MIDDLE_2
        );

        when(userRepository.existsByEmailIgnoreCase(request.email()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SignupResponse response = signupService.signup(request);

        // then
        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail())
                .isEqualTo("student01@example.com");

        assertThat(savedUser.getPasswordHash())
                .isEqualTo("encoded-password");

        assertThat(savedUser.getNickname())
                .isEqualTo("학생01");

        assertThat(savedUser.getRole())
                .isEqualTo(Role.USER);

        assertThat(savedUser.getStatus())
                .isEqualTo(UserStatus.ACTIVE);

        ArgumentCaptor<StudentProfile> profileCaptor =
                ArgumentCaptor.forClass(StudentProfile.class);

        verify(studentProfileRepository)
                .save(profileCaptor.capture());

        StudentProfile savedProfile = profileCaptor.getValue();

        assertThat(savedProfile.getUser())
                .isSameAs(savedUser);

        assertThat(savedProfile.getGrade())
                .isEqualTo(Grade.MIDDLE_2);

        assertThat(response.email())
                .isEqualTo("student01@example.com");

        assertThat(response.nickname())
                .isEqualTo("학생01");

        assertThat(response.grade())
                .isEqualTo(Grade.MIDDLE_2);
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 회원가입을 거부한다")
    void signupFailsWhenEmailAlreadyExists() {
        // given
        SignupRequest request = new SignupRequest(
                "student01@example.com",
                "Password123!",
                "학생01",
                Grade.MIDDLE_2
        );

        when(userRepository.existsByEmailIgnoreCase(request.email()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));

        verify(studentProfileRepository, never())
                .save(any(StudentProfile.class));
    }
}