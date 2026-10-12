package com.studybridge.edutech.identity.application.query;

import com.studybridge.edutech.identity.api.query.dto.MeResponse;
import com.studybridge.edutech.identity.application.exception.UserNotFoundException;
import com.studybridge.edutech.identity.domain.Role;
import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.identity.infrastructure.UserRepository;
import com.studybridge.edutech.student.domain.Grade;
import com.studybridge.edutech.student.domain.StudentProfile;
import com.studybridge.edutech.student.infrastructure.StudentProfileRepository;
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
 * 내 정보 조회 Use Case(MeQueryService)의 로직을 검증합니다.
 *
 * <p>Spring을 띄우지 않는 순수 단위 테스트입니다.
 * Repository는 Mockito Mock으로 대체하여 DB 없이 실행됩니다.</p>
 */
@ExtendWith(MockitoExtension.class) // @Mock, @InjectMocks를 동작하게 해주는 JUnit 확장
class MeQueryServiceTest {

    // 가짜(Mock) Repository: 우리가 when(...)으로 정한 값만 돌려준다
    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    // 위의 Mock 두 개를 생성자에 넣어서 진짜 MeQueryService를 만들어준다
    @InjectMocks
    private MeQueryService meQueryService;

    @Test
    @DisplayName("학생 프로필이 있으면 학년을 포함한 내 정보를 반환한다")
    void getMe_withStudentProfile() {
        // given: 학생 사용자 + 중2 프로필이 DB에 있다고 가정
        UUID userId = UUID.randomUUID();

        User user = User.createLocal(
                "student01@example.com",
                "encoded-password",
                "학생01"
        );
        StudentProfile profile = StudentProfile.create(user, Grade.MIDDLE_2);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(profile));

        // when
        MeResponse response = meQueryService.getMe(userId);

        // then
        // id는 검사하지 않는다:
        // DB에 저장(save)하지 않은 User라서 JPA가 UUID를 만들어주지 않아 null이기 때문.
        // id가 응답에 잘 나가는지는 다음 Controller 테스트에서 확인한다.
        assertThat(response.email()).isEqualTo("student01@example.com");
        assertThat(response.nickname()).isEqualTo("학생01");
        assertThat(response.grade()).isEqualTo(Grade.MIDDLE_2);
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.linkedAccounts()).isEmpty(); // OAuth 연동 전이라 항상 빈 목록
    }

    @Test
    @DisplayName("학생 프로필이 없으면 grade는 null로 반환한다")
    void getMe_withoutStudentProfile() {
        // given: User는 있지만 StudentProfile은 없는 경우 (예: 관리자 계정)
        UUID userId = UUID.randomUUID();

        User user = User.createLocal(
                "admin01@example.com",
                "encoded-password",
                "관리자01"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(userId))
                .thenReturn(Optional.empty()); // 프로필 없음

        // when
        MeResponse response = meQueryService.getMe(userId);

        // then: 에러가 아니라, grade만 비어 있는 정상 응답이어야 한다
        assertThat(response.nickname()).isEqualTo("관리자01");
        assertThat(response.grade()).isNull();
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 UserNotFoundException이 발생한다")
    void getMe_userNotFound() {
        // given: 토큰은 유효했지만, 그 사이 계정이 삭제된 상황
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> meQueryService.getMe(userId))
                .isInstanceOf(UserNotFoundException.class);

        // 사용자가 없으면 거기서 바로 멈춰야 하므로, 프로필 조회는 한 번도 호출되면 안 된다
        verify(studentProfileRepository, never()).findByUserId(any());
    }
}