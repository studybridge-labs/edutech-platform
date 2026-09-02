package com.studybridge.edutech.curriculum.application.exception;

/**
 * 비활성화된 문제에 대해 허용되지 않는 작업을 시도할 때 발생합니다.
 */
public class QuestionInactiveException extends RuntimeException {

    public QuestionInactiveException() {
        super("비활성화된 문제입니다.");
    }
}