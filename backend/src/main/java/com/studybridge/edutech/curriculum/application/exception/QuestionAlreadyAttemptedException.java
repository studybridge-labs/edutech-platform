package com.studybridge.edutech.curriculum.application.exception;

/**
 * 이미 학습 기록이 존재하는 문제의 정답·선택지·내용을
 * 변경하려 할 때 발생합니다.
 *
 * <p>기존 학습 데이터의 정합성을 보호하기 위해 변경을 제한합니다.</p>
 */
public class QuestionAlreadyAttemptedException extends RuntimeException {

    public QuestionAlreadyAttemptedException() {
        super("이미 학습 기록이 있는 문제는 수정할 수 없습니다.");
    }
}