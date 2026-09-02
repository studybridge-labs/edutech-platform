package com.studybridge.edutech.curriculum.infrastructure;

import com.studybridge.edutech.curriculum.domain.Passage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Passage Entity의 영속성 처리를 담당하는 Repository입니다.
 *
 * <p>단원에 속한 독해 지문을 조회하거나, 관리자가 지문을 관리할 때 사용합니다.</p>
 */
public interface PassageRepository extends JpaRepository<Passage, UUID> {

    /**
     * 특정 단원에 속한 활성 지문 목록을 조회합니다.
     *
     * @param unitId 단원 식별자
     * @return 활성 지문 목록
     */
    List<Passage> findByUnitIdAndActiveTrue(UUID unitId);
}