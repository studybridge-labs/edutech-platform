package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.identity.api.command.dto.UpdateProfileRequest;
import com.studybridge.edutech.identity.api.query.dto.MeResponse;
import com.studybridge.edutech.identity.application.exception.StudentProfileNotFoundException;
import com.studybridge.edutech.identity.application.exception.UserNotFoundException;
import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.identity.infrastructure.UserRepository;
import com.studybridge.edutech.student.domain.Grade;
import com.studybridge.edutech.student.domain.StudentProfile;
import com.studybridge.edutech.student.infrastructure.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 프로필 변경 Use Case(ProfileService)의 로직을 검증합니다.
 *
 * <p>Spring과 DB 없이 실행되는 순수 단위 테스트입니다.
 * 응답 값뿐 아니라 Entity 자체의 상태도 확인합니다.
 * (실제 서비스에서는 바뀐 Entity를 JPA가 감지해서 UPDATE하기 때문)</p>
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId;
    private User user;
    private StudentProfile studentProfile;

    @BeforeEach
    void setUp() {
        // 매 테스트마다 새 객체로 시작한다 (앞 테스트에서 바뀐 닉네임이 남지 않도록)
        // 주의: 여기서는 객체만 만들고 when(...) 설정은 하지 않는다. (아래 💡 1번 참고)
        userId = UUID.randomUUID();

        user = User.createLocal(
                "student01@example.com",
                "encoded-password",
                "학생01"
        );

        studentProfile = StudentProfile.create(user, Grade.MIDDLE_2);
    }

    @Test
    @DisplayName("닉네임만 보내면 닉네임만 변경되고 학년은 유지된다")
    void updateProfile_nicknameOnly() {
        // given
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(studentProfile));

        UpdateProfileRequest request = new UpdateProfileRequest("새닉네임", null);

        // when
        MeResponse response = profileService.updateProfile(userId, request);

        // then: 응답
        assertThat(response.nickname()).isEqualTo("새닉네임");
        assertThat(response.grade()).isEqualTo(Grade.MIDDLE_2);

        // then: Entity 상태 (실제로는 이 값이 커밋 시 UPDATE된다)
        assertThat(user.getNickname()).isEqualTo("새닉네임");
        assertThat(studentProfile.getGrade()).isEqualTo(Grade.MIDDLE_2);
    }

    @Test
    @DisplayName("학년만 보내면 학년만 변경되고 닉네임은 유지된다")
    void updateProfile_gradeOnly() {
        // given
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(studentProfile));

        UpdateProfileRequest request = new UpdateProfileRequest(null, Grade.MIDDLE_3);

        // when
        MeResponse response = profileService.updateProfile(userId, request);

        // then
        assertThat(response.nickname()).isEqualTo("학생01");
        assertThat(response.grade()).isEqualTo(Grade.MIDDLE_3);

        assertThat(user.getNickname()).isEqualTo("학생01");
        assertThat(studentProfile.getGrade()).isEqualTo(Grade.MIDDLE_3);
    }

    @Test
    @DisplayName("닉네임과 학년을 모두 보내면 둘 다 변경된다")
    void updateProfile_both() {
        // given
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(studentProfile));

        UpdateProfileRequest request = new UpdateProfileRequest("새닉네임", Grade.MIDDLE_3);

        // when
        MeResponse response = profileService.updateProfile(userId, request);

        // then
        assertThat(response.nickname()).isEqualTo("새닉네임");
        assertThat(response.grade()).isEqualTo(Grade.MIDDLE_3);

        assertThat(user.getNickname()).isEqualTo("새닉네임");
        assertThat(studentProfile.getGrade()).isEqualTo(Grade.MIDDLE_3);
    }

    @Test
    @DisplayName("빈 요청이면 아무것도 변경하지 않고 현재 정보를 반환한다")
    void updateProfile_emptyRequest() {
        // given
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(studentProfile));

        UpdateProfileRequest request = new UpdateProfileRequest(null, null); // {} 와 같음

        // when
        MeResponse response = profileService.updateProfile(userId, request);

        // then: 에러가 아니라 현재 상태 그대로
        assertThat(response.nickname()).isEqualTo("학생01");
        assertThat(response.grade()).isEqualTo(Grade.MIDDLE_2);
    }

    @Test
    @DisplayName("학생 프로필이 없는 계정이 학년을 보내면 예외가 발생하고 닉네임도 변경되지 않는다")
    void updateProfile_gradeWithoutStudentProfile() {
        // given: User는 있지만 StudentProfile이 없는 계정 (예: 관리자)
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        // 닉네임과 학년을 같이 보낸다
        UpdateProfileRequest request = new UpdateProfileRequest("새닉네임", Grade.MIDDLE_3);

        // when & then
        assertThatThrownBy(() -> profileService.updateProfile(userId, request))
                .isInstanceOf(StudentProfileNotFoundException.class);

        // 학년 검사가 닉네임 변경보다 먼저 실행되므로, 닉네임은 그대로여야 한다
        assertThat(user.getNickname()).isEqualTo("학생01");
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 UserNotFoundException이 발생한다")
    void updateProfile_userNotFound() {
        // given
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        UpdateProfileRequest request = new UpdateProfileRequest("새닉네임", null);

        // when & then
        assertThatThrownBy(() -> profileService.updateProfile(userId, request))
                .isInstanceOf(UserNotFoundException.class);

        // 사용자가 없으면 바로 멈추므로 프로필 조회는 호출되지 않아야 한다
        verify(studentProfileRepository, never()).findByUserId(any());
    }
}