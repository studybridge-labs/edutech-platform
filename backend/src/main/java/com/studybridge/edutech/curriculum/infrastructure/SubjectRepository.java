package com.studybridge.edutech.curriculum.infrastructure;

import com.studybridge.edutech.curriculum.domain.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Subject Entity의 영속성 처리를 담당하는 Repository입니다.
 *
 * <p>학생에게 과목 목록을 노출하거나, 관리자가 콘텐츠를 등록할 때
 * 과목을 조회하는 데 사용합니다.</p>
 */
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    /**
     * 과목 코드로 과목을 조회합니다.
     *
     * @param code 과목 코드 (예: MATH)
     * @return 조회된 Subject (없으면 빈 Optional)
     */
    Optional<Subject> findByCode(String code);

    /**
     * 활성 상태인 과목 목록을 정렬하여 조회합니다.
     *
     * <p>학생용 과목 목록 노출에 사용합니다.</p>
     *
     * @return 활성 과목 목록
     */
    List<Subject> findByActiveTrueOrderByName();
}