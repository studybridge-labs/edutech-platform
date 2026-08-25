package com.studybridge.edutech.global.exception;

import com.studybridge.edutech.identity.application.exception.EmailAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

/**
 * 애플리케이션 전역에서 발생하는 예외를
 * 공통 API Error Response로 변환합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 이미 사용 중인 이메일로 회원가입을 시도한 경우 처리합니다.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {
        String traceId = createTraceId();

        log.warn(
                "회원가입 이메일 중복 오류. traceId={}",
                traceId
        );

        ErrorCode errorCode = ErrorCode.EMAIL_ALREADY_EXISTS;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, traceId));
    }

    /**
     * @Valid 검증에 실패한 요청을 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String traceId = createTraceId();

        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ErrorCode.VALIDATION_ERROR.getMessage());

        log.warn(
                "요청 Validation 오류. traceId={}, message={}",
                traceId,
                message
        );

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(
                        ErrorResponse.of(
                                errorCode,
                                message,
                                traceId
                        )
                );
    }

    /**
     * 오류 추적을 위한 식별자를 생성합니다.
     */
    private String createTraceId() {
        return UUID.randomUUID().toString();
    }
}