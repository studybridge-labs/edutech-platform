package com.studybridge.edutech.student.infrastructure;

import com.studybridge.edutech.student.domain.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * StudentProfile Entity의 영속성 처리를 담당하는 Repository입니다.
 */
public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    /**
     * 사용자 ID로 학생 프로필을 조회합니다.
     *
     * <p>메서드 이름 규칙(Query Method)으로 쿼리가 자동 생성됩니다.
     * findBy + User(필드) + Id(그 필드의 id) → WHERE user_id = ?</p>
     *
     * @param userId 사용자 ID
     * @return 학생 프로필. 없으면(ADMIN 등) Optional.empty()
     */
    Optional<StudentProfile> findByUserId(UUID userId);
}