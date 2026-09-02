package com.studybridge.edutech.curriculum.application.exception;

import java.util.UUID;

/**
 * 존재하지 않는 과목을 조회하려 할 때 발생합니다.
 */
public class SubjectNotFoundException extends RuntimeException {

    public SubjectNotFoundException() {
        super("과목을 찾을 수 없습니다.");
    }

    public SubjectNotFoundException(UUID subjectId) {
        super("과목을 찾을 수 없습니다. subjectId=" + subjectId);
    }
}