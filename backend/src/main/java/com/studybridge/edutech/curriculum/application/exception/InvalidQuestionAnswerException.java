package com.studybridge.edutech.curriculum.application.exception;

/**
 * 문제 유형에 맞지 않는 답안이 제출되었을 때 발생합니다.
 *
 * <p>예를 들어 객관식 문제에 선택지 없이 답안 문자열만 전달되거나,
 * 단답형 문제에 선택지 식별자가 전달된 경우입니다.</p>
 */
public class InvalidQuestionAnswerException extends RuntimeException {

    public InvalidQuestionAnswerException() {
        super("문제 유형에 맞지 않는 답안입니다.");
    }
}