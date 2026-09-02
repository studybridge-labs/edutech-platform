package com.studybridge.edutech.curriculum.domain;

/**
 * 문제의 난이도를 정의합니다.
 *
 * <p>학습 결과에 따라 적절한 난이도의 문제를 추천하는 데 사용됩니다.</p>
 */
public enum Difficulty {

    /**
     * 쉬운 난이도입니다.
     *
     * <p>정답률이 낮은 학생에게 우선적으로 추천됩니다.</p>
     */
    EASY,

    /**
     * 보통 난이도입니다.
     */
    MEDIUM,

    /**
     * 어려운 난이도입니다.
     *
     * <p>정답률이 높은 학생에게 추천됩니다.</p>
     */
    HARD
}