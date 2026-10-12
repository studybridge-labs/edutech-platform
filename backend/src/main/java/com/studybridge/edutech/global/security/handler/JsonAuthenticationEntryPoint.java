package com.studybridge.edutech.global.security.handler;

import com.studybridge.edutech.global.exception.ErrorCode;
import com.studybridge.edutech.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 인증되지 않은 요청이 보호된 API에 접근했을 때
 * 401 Unauthorized를 공통 ErrorResponse(JSON) 형식으로 응답합니다.
 *
 * <p>호출되는 경우</p>
 * <ul>
 *   <li>Authorization 헤더 없이 보호된 API 요청</li>
 *   <li>유효하지 않거나 만료된 Access Token으로 요청
 *       (JWT 필터가 인증 정보를 비워 두므로 "인증 안 됨"과 같은 상태)</li>
 * </ul>
 *
 * <p>GlobalExceptionHandler(@RestControllerAdvice)는 Controller 안의 예외만 처리하므로,
 * Controller에 도달하기 전인 Security 필터 단계의 인증 실패는 여기서 처리합니다.</p>
 *
 * <p>@Component로 등록하지 않고 SecurityConfig에서 직접 생성합니다.
 * (JwtAuthenticationFilter와 같은 방식. @WebMvcTest에서 별도 @Import 없이 동작)</p>
 */
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log =
            LoggerFactory.getLogger(JsonAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        String traceId = UUID.randomUUID().toString();
        ErrorCode errorCode = ErrorCode.ACCESS_TOKEN_INVALID;

        /*
         * 상세 원인은 응답이 아니라 서버 로그에만 남긴다.
         * traceId로 "어떤 요청이 왜 실패했는지" 추적할 수 있다.
         * 토큰 원문은 로그에 남기지 않는다.
         */
        log.info(
                "인증 실패. traceId={}, method={}, uri={}",
                traceId,
                request.getMethod(),
                request.getRequestURI()
        );

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 한글 메시지가 깨지지 않도록 UTF-8을 명시한다.
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
                response.getOutputStream(),
                ErrorResponse.of(errorCode, traceId)
        );
    }
}