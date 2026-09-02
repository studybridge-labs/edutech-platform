package com.studybridge.edutech.curriculum.application.exception;

import java.util.UUID;

/**
 * 존재하지 않는 문제를 조회하려 할 때 발생합니다.
 */
public class QuestionNotFoundException extends RuntimeException {

    public QuestionNotFoundException() {
        super("문제를 찾을 수 없습니다.");
    }

    public QuestionNotFoundException(UUID questionId) {
        super("문제를 찾을 수 없습니다. questionId=" + questionId);
    }
}