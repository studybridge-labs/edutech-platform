package com.studybridge.edutech.curriculum.domain;

/**
 * 문제의 출제 형태를 정의합니다.
 *
 * <p>문제 형태에 따라 답안 제출 방식과 채점 방식이 달라집니다.</p>
 */
public enum QuestionType {

    /**
     * 여러 선택지 중 하나를 고르는 객관식 문제입니다.
     *
     * <p>선택한 선택지의 정답 여부로 채점합니다.</p>
     */
    MULTIPLE_CHOICE,

    /**
     * 직접 답을 입력하는 단답형 문제입니다.
     *
     * <p>입력한 답안 문자열과 정답을 비교하여 채점합니다.</p>
     */
    SHORT_ANSWER
}