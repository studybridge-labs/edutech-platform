package com.studybridge.edutech.global.exception;

import com.studybridge.edutech.identity.application.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
     * 요청 본문(JSON)을 DTO로 변환하지 못한 경우를 처리합니다.
     *
     * <p>@Valid 검증보다 앞 단계에서 발생합니다. 예:</p>
     * <ul>
     *     <li>enum에 없는 값: {"grade": "MIDDLE_9"}</li>
     *     <li>JSON 문법 오류: {"nickname": "abc"   (닫는 괄호 누락)</li>
     *     <li>본문 없이 요청</li>
     * </ul>
     *
     * <p>이 핸들러가 없으면 Spring 기본 처리로 넘어가 /error로 재요청되고,
     * 그 과정에서 Security에 막혀 400이 아닌 401로 응답되는 문제가 있었습니다. (#11)</p>
     *
     * <p>응답 메시지에는 내부 정보(클래스명, 파싱 위치 등)를 노출하지 않고,
     * 자세한 원인은 서버 로그에만 남깁니다.</p>
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException exception
    ) {
        String traceId = createTraceId();

        // 원인 중 가장 안쪽(가장 구체적인) 예외의 메시지를 로그에 남긴다
        // 예: Cannot deserialize value of type `Grade` from String "MIDDLE_9" ...
        log.warn(
                "요청 본문 변환 실패. traceId={}, reason={}",
                traceId,
                exception.getMostSpecificCause().getMessage()
        );

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(
                        ErrorResponse.of(
                                errorCode,
                                "요청 본문의 형식이 올바르지 않습니다.",
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
     * 요청한 사용자를 찾을 수 없는 경우 처리합니다.
     * (예: Access Token은 유효하지만 그 사이 사용자가 삭제된 경우)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException exception
    ) {
        String traceId = createTraceId();

        log.warn(
                "사용자 조회 실패. traceId={}",
                traceId
        );

        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, traceId));
    }

    /**
     * 학생 프로필이 필요한 작업인데 학생 프로필이 없는 경우 처리합니다.
     * (예: 관리자 계정이 학년 변경을 요청한 경우)
     */
    @ExceptionHandler(StudentProfileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStudentProfileNotFound(
            StudentProfileNotFoundException exception
    ) {
        String traceId = createTraceId();

        log.warn(
                "학생 프로필 조회 실패. traceId={}",
                traceId
        );

        ErrorCode errorCode = ErrorCode.STUDENT_PROFILE_NOT_FOUND;

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