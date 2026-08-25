package com.studybridge.edutech.global.exception;

/**
 * API 오류 발생 시 Client에 반환하는 공통 응답 형식입니다.
 *
 * @param status  HTTP 상태 코드
 * @param code    서비스 오류 코드
 * @param message 사용자에게 전달할 오류 메시지
 * @param traceId 오류 추적을 위한 식별자
 */
public record ErrorResponse(
        int status,
        String code,
        String message,
        String traceId
) {

    /**
     * ErrorCode를 기반으로 공통 오류 응답을 생성합니다.
     *
     * @param errorCode 오류 코드
     * @param traceId   요청 추적 식별자
     * @return 생성된 오류 응답
     */
    public static ErrorResponse of(
            ErrorCode errorCode,
            String traceId
    ) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                traceId
        );
    }

    /**
     * ErrorCode의 기본 메시지 대신 별도의 메시지를 사용하여 응답을 생성합니다.
     */
    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            String traceId
    ) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message,
                traceId
        );
    }
}