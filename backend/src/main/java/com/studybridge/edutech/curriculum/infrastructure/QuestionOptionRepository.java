package com.studybridge.edutech.curriculum.infrastructure;

import com.studybridge.edutech.curriculum.domain.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * QuestionOption Entity의 영속성 처리를 담당하는 Repository입니다.
 *
 * <p>선택지는 대부분 Question Aggregate를 통해 접근하지만,
 * 채점 시 선택지 단독 조회가 필요한 경우를 위해 제공합니다.</p>
 */
public interface QuestionOptionRepository
        extends JpaRepository<QuestionOption, UUID> {

    /**
     * 특정 문제에 속한 선택지 목록을 번호 순서대로 조회합니다.
     *
     * @param questionId 문제 식별자
     * @return 정렬된 선택지 목록
     */
    List<QuestionOption> findByQuestionIdOrderByOptionNo(UUID questionId);
}