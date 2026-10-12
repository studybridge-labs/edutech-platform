package com.studybridge.edutech.identity.application.query;

import com.studybridge.edutech.identity.api.query.dto.MeResponse;
import com.studybridge.edutech.identity.application.exception.UserNotFoundException;
import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.identity.infrastructure.UserRepository;
import com.studybridge.edutech.student.domain.Grade;
import com.studybridge.edutech.student.domain.StudentProfile;
import com.studybridge.edutech.student.infrastructure.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 로그인한 사용자 본인의 정보를 조회합니다. (QUERY)
 *
 * <p>상태를 변경하지 않는 조회 전용 Service이므로
 * readOnly 트랜잭션으로 실행합니다. (architecture.md 7. Transaction)</p>
 */
@Service
public class MeQueryService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    public MeQueryService(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository
    ) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    /**
     * 사용자 ID로 내 정보를 조회합니다.
     *
     * <p>userId는 반드시 Access Token에서 꺼낸 값(AuthUser)을 사용합니다.
     * 클라이언트가 보낸 값을 받지 않으므로 다른 사용자의 정보는 조회할 수 없습니다.</p>
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 내 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public MeResponse getMe(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 학생이 아니면(ADMIN 등) StudentProfile이 없으므로 grade는 null
        Grade grade = studentProfileRepository.findByUserId(userId)
                .map(StudentProfile::getGrade)
                .orElse(null);

        return MeResponse.of(user, grade);
    }
}