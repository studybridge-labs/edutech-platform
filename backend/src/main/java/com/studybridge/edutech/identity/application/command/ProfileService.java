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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 프로필 변경 Use Case를 처리합니다. (COMMAND)
 *
 * <p>닉네임(User)과 학년(StudentProfile)을 하나의 Transaction 안에서 변경합니다.
 * 요청에서 null인 항목은 변경하지 않습니다. (PATCH)</p>
 */
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    public ProfileService(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository
    ) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    /**
     * 로그인한 사용자의 프로필을 변경하고, 변경된 최신 정보를 반환합니다.
     *
     * @param userId  Access Token에서 꺼낸 사용자 ID (요청 본문의 값이 아님)
     * @param request 변경할 항목 (null인 항목은 변경 안 함)
     * @return 변경 후의 내 정보 (GET /me와 같은 형태)
     */
    @Transactional
    public MeResponse updateProfile(
            UUID userId,
            UpdateProfileRequest request
    ) {
        // 1. 변경 대상 조회
        //    여기서 조회한 Entity는 JPA가 "조회 당시 상태"를 기억해 둔다 (변경 감지 준비)
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Optional<StudentProfile> studentProfile =
                studentProfileRepository.findByUserId(userId);

        // 2. 학년 변경 (실패할 수 있는 작업을 먼저 처리)
        //    학생 프로필이 없는 계정(관리자 등)이 학년을 보냈다면, 아무것도 바꾸기 전에 실패시킨다
        if (request.grade() != null) {
            StudentProfile profile = studentProfile
                    .orElseThrow(StudentProfileNotFoundException::new);

            profile.changeGrade(request.grade());
        }

        // 3. 닉네임 변경
        if (request.nickname() != null) {
            user.changeNickname(request.nickname());
        }

        // save()를 호출하지 않는다.
        // 메서드가 끝나고 Transaction이 커밋될 때, JPA가 바뀐 필드를 감지해서
        // UPDATE 쿼리를 자동으로 실행한다. (변경 감지, Dirty Checking)

        // 4. 변경된 최신 상태로 응답 생성
        //    위에서 changeGrade()로 바꾼 바로 그 객체이므로, 새 학년이 들어있다
        Grade grade = studentProfile
                .map(StudentProfile::getGrade)
                .orElse(null);

        return MeResponse.of(user, grade);
    }
}