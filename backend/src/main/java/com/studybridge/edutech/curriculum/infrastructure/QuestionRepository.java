package com.studybridge.edutech.curriculum.infrastructure;

import com.studybridge.edutech.curriculum.domain.Difficulty;
import com.studybridge.edutech.curriculum.domain.Question;
import com.studybridge.edutech.curriculum.domain.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Question Entity의 영속성 처리를 담당하는 Repository입니다.
 *
 * <p>단원별 문제 목록을 난이도와 유형으로 필터링하여 조회하고,
 * 관리자 문제 관리에 필요한 조회를 제공합니다.</p>
 */
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    /**
     * 특정 단원의 활성 문제를 난이도와 유형으로 필터링하여 페이지 단위로 조회합니다.
     *
     * <p>{@code difficulty}와 {@code questionType}은 선택적 조건으로,
     * null이면 해당 조건을 적용하지 않습니다.</p>
     *
     * @param unitId       단원 식별자
     * @param difficulty   난이도 필터 (없으면 null)
     * @param questionType 유형 필터 (없으면 null)
     * @param pageable     페이지 정보
     * @return 조건에 맞는 문제 페이지
     */
    @Query("""
            SELECT q FROM Question q
            WHERE q.unit.id = :unitId
              AND q.active = true
              AND (:difficulty IS NULL OR q.difficulty = :difficulty)
              AND (:questionType IS NULL OR q.questionType = :questionType)
            """)
    Page<Question> findActiveByUnitWithFilters(
            @Param("unitId") UUID unitId,
            @Param("difficulty") Difficulty difficulty,
            @Param("questionType") QuestionType questionType,
            Pageable pageable
    );

    /**
     * 선택지를 함께 로딩하여 단일 문제를 조회합니다.
     *
     * <p>문제 상세 조회나 채점 시 선택지가 필요하므로 fetch join으로 함께 가져옵니다.
     * 존재하지 않으면 빈 Optional을 반환합니다.</p>
     *
     * @param questionId 문제 식별자
     * @return 선택지가 로딩된 Question (없으면 빈 Optional)
     */
    @Query("""
            SELECT q FROM Question q
            LEFT JOIN FETCH q.options
            WHERE q.id = :questionId
            """)
    java.util.Optional<Question> findByIdWithOptions(
            @Param("questionId") UUID questionId
    );
}