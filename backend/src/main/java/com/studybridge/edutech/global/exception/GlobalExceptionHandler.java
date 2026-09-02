package com.studybridge.edutech.global.exception;

import com.studybridge.edutech.curriculum.application.exception.InvalidQuestionAnswerException;
import com.studybridge.edutech.curriculum.application.exception.PassageNotFoundException;
import com.studybridge.edutech.curriculum.application.exception.QuestionAlreadyAttemptedException;
import com.studybridge.edutech.curriculum.application.exception.QuestionInactiveException;
import com.studybridge.edutech.curriculum.application.exception.QuestionNotFoundException;
import com.studybridge.edutech.curriculum.application.exception.SubjectNotFoundException;
import com.studybridge.edutech.curriculum.application.exception.UnitNotFoundException;
import com.studybridge.edutech.identity.application.exception.AccountNotActiveException;
import com.studybridge.edutech.identity.application.exception.EmailAlreadyExistsException;
import com.studybridge.edutech.identity.application.exception.InvalidCredentialsException;
import com.studybridge.edutech.identity.application.exception.InvalidRefreshTokenException;
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

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        String traceId = UUID.randomUUID().toString();

        ErrorCode errorCode =
                ErrorCode.INVALID_CREDENTIALS;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(
                        ErrorResponse.of(
                                errorCode,
                                traceId
                        )
                );
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotActive(
            AccountNotActiveException exception
    ) {
        String traceId = UUID.randomUUID().toString();

        ErrorCode errorCode =
                ErrorCode.ACCOUNT_NOT_ACTIVE;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(
                        ErrorResponse.of(
                                errorCode,
                                traceId
                        )
                );
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException exception
    ) {
        String traceId = UUID.randomUUID().toString();

        ErrorCode errorCode =
                ErrorCode.INVALID_REFRESH_TOKEN;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(
                        ErrorResponse.of(
                                errorCode,
                                traceId
                        )
                );
    }

    /**
     * 존재하지 않는 과목을 조회한 경우 처리합니다.
     */
    @ExceptionHandler(SubjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSubjectNotFound(
            SubjectNotFoundException exception
    ) {
        return notFound(ErrorCode.SUBJECT_NOT_FOUND, exception);
    }

    /**
     * 존재하지 않는 단원을 조회한 경우 처리합니다.
     */
    @ExceptionHandler(UnitNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnitNotFound(
            UnitNotFoundException exception
    ) {
        return notFound(ErrorCode.UNIT_NOT_FOUND, exception);
    }

    /**
     * 존재하지 않는 지문을 조회한 경우 처리합니다.
     */
    @ExceptionHandler(PassageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePassageNotFound(
            PassageNotFoundException exception
    ) {
        return notFound(ErrorCode.PASSAGE_NOT_FOUND, exception);
    }

    /**
     * 존재하지 않는 문제를 조회한 경우 처리합니다.
     */
    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleQuestionNotFound(
            QuestionNotFoundException exception
    ) {
        return notFound(ErrorCode.QUESTION_NOT_FOUND, exception);
    }

    /**
     * 비활성화된 문제에 접근한 경우 처리합니다.
     */
    @ExceptionHandler(QuestionInactiveException.class)
    public ResponseEntity<ErrorResponse> handleQuestionInactive(
            QuestionInactiveException exception
    ) {
        String traceId = createTraceId();

        ErrorCode errorCode = ErrorCode.QUESTION_INACTIVE;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, traceId));
    }

    /**
     * 문제 유형에 맞지 않는 답안이 제출된 경우 처리합니다.
     */
    @ExceptionHandler(InvalidQuestionAnswerException.class)
    public ResponseEntity<ErrorResponse> handleInvalidQuestionAnswer(
            InvalidQuestionAnswerException exception
    ) {
        String traceId = createTraceId();

        ErrorCode errorCode = ErrorCode.INVALID_QUESTION_ANSWER;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, traceId));
    }

    /**
     * 이미 학습 기록이 있는 문제를 수정하려 한 경우 처리합니다.
     */
    @ExceptionHandler(QuestionAlreadyAttemptedException.class)
    public ResponseEntity<ErrorResponse> handleQuestionAlreadyAttempted(
            QuestionAlreadyAttemptedException exception
    ) {
        String traceId = createTraceId();

        log.warn(
                "학습 기록이 있는 문제 수정 시도. traceId={}",
                traceId
        );

        ErrorCode errorCode = ErrorCode.QUESTION_ALREADY_ATTEMPTED;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, traceId));
    }

    /**
     * 리소스를 찾을 수 없는 예외에 대한 공통 응답을 생성합니다.
     */
    private ResponseEntity<ErrorResponse> notFound(
            ErrorCode errorCode,
            RuntimeException exception
    ) {
        String traceId = createTraceId();

        log.warn(
                "리소스 조회 실패. traceId={}, code={}, detail={}",
                traceId,
                errorCode.getCode(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, traceId));
    }

    /**
     * 오류 추적을 위한 식별자를 생성합니다.
     */
    private String createTraceId() {
        return UUID.randomUUID().toString();
    }
}