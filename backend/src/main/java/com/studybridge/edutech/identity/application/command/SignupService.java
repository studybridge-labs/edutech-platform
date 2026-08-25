package com.studybridge.edutech.identity.application.command;

import com.studybridge.edutech.identity.api.command.dto.SignupRequest;
import com.studybridge.edutech.identity.api.command.dto.SignupResponse;
import com.studybridge.edutech.identity.application.exception.EmailAlreadyExistsException;
import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.identity.infrastructure.UserRepository;
import com.studybridge.edutech.student.domain.StudentProfile;
import com.studybridge.edutech.student.infrastructure.StudentProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * LOCAL 회원가입 Use Case를 처리합니다.
 *
 * <p>이메일 중복 검증, 비밀번호 암호화,
 * 사용자 및 학생 프로필 생성을 하나의 Transaction으로 처리합니다.</p>
 */
@Service
public class SignupService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 새로운 LOCAL 사용자를 등록합니다.
     *
     * <p>User와 StudentProfile은 하나의 회원가입 작업이므로
     * 둘 중 하나라도 저장에 실패하면 전체 작업을 Rollback합니다.</p>
     *
     * @param request 회원가입 요청
     * @return 생성된 사용자 정보
     * @throws EmailAlreadyExistsException 이미 사용 중인 이메일인 경우
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        validateDuplicateEmail(request.email());

        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.createLocal(
                request.email(),
                passwordHash,
                request.nickname()
        );

        User savedUser = userRepository.save(user);

        StudentProfile studentProfile = StudentProfile.create(
                savedUser,
                request.grade()
        );

        StudentProfile savedStudentProfile =
                studentProfileRepository.save(studentProfile);

        return SignupResponse.from(
                savedUser,
                savedStudentProfile
        );
    }

    /**
     * 이메일 중복 여부를 확인합니다.
     */
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException();
        }
    }
}