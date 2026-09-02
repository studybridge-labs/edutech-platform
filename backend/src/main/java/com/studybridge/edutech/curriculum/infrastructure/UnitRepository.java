package com.studybridge.edutech.curriculum.infrastructure;

import com.studybridge.edutech.curriculum.domain.Unit;
import com.studybridge.edutech.student.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Unit Entity의 영속성 처리를 담당하는 Repository입니다.
 *
 * <p>과목과 학년을 기준으로 단원 목록을 조회하고,
 * 계층 구조에서 하위 단원을 조회하는 데 사용합니다.</p>
 */
public interface UnitRepository extends JpaRepository<Unit, UUID> {

    /**
     * 특정 과목과 학년에 속한 활성 단원 목록을 정렬 순서대로 조회합니다.
     *
     * <p>학생용 단원 목록 노출에 사용합니다.</p>
     *
     * @param subjectId 과목 식별자
     * @param grade     대상 학년
     * @return 정렬된 활성 단원 목록
     */
    List<Unit> findBySubjectIdAndGradeAndActiveTrueOrderByOrderNo(
            UUID subjectId,
            Grade grade
    );

    /**
     * 특정 상위 단원에 속한 활성 하위 단원 목록을 정렬 순서대로 조회합니다.
     *
     * @param parentUnitId 상위 단원 식별자
     * @return 정렬된 활성 하위 단원 목록
     */
    List<Unit> findByParentUnitIdAndActiveTrueOrderByOrderNo(UUID parentUnitId);

    /**
     * 특정 과목에 속한 활성 최상위 단원 목록을 정렬 순서대로 조회합니다.
     *
     * @param subjectId 과목 식별자
     * @return 정렬된 활성 최상위 단원 목록
     */
    List<Unit> findBySubjectIdAndParentUnitIsNullAndActiveTrueOrderByOrderNo(
            UUID subjectId
    );
}