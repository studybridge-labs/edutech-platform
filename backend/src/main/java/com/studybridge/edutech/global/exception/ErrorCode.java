package com.studybridge.edutech.global.exception;

import org.springframework.http.HttpStatus;

/**
 * API에서 사용하는 공통 오류 코드를 정의합니다.
 *
 * <p>HTTP 상태 코드와 서비스 내부 오류 코드를 함께 관리하여
 * Client가 오류의 종류를 명확하게 구분할 수 있도록 합니다.</p>
 */
public enum ErrorCode {

    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "입력값을 확인해주세요."
    ),

    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "EMAIL_ALREADY_EXISTS",
            "이미 사용 중인 이메일입니다."
    ),

    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "INVALID_CREDENTIALS",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    ACCOUNT_NOT_ACTIVE(
            HttpStatus.FORBIDDEN,
            "ACCOUNT_NOT_ACTIVE",
            "현재 사용할 수 없는 계정입니다."
    ),

    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "INVALID_REFRESH_TOKEN",
            "유효하지 않은 인증 세션입니다."
    ),

    // ===== Curriculum =====

    SUBJECT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SUBJECT_NOT_FOUND",
            "과목을 찾을 수 없습니다."
    ),

    UNIT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "UNIT_NOT_FOUND",
            "단원을 찾을 수 없습니다."
    ),

    PASSAGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PASSAGE_NOT_FOUND",
            "지문을 찾을 수 없습니다."
    ),

    QUESTION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "QUESTION_NOT_FOUND",
            "문제를 찾을 수 없습니다."
    ),

    QUESTION_INACTIVE(
            HttpStatus.CONFLICT,
            "QUESTION_INACTIVE",
            "비활성화된 문제입니다."
    ),

    INVALID_QUESTION_ANSWER(
            HttpStatus.BAD_REQUEST,
            "INVALID_QUESTION_ANSWER",
            "문제 유형에 맞지 않는 답안입니다."
    ),

    QUESTION_ALREADY_ATTEMPTED(
            HttpStatus.CONFLICT,
            "QUESTION_ALREADY_ATTEMPTED",
            "이미 학습 기록이 있는 문제는 수정할 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}