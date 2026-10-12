package com.studybridge.edutech.global.security.handler;

import com.studybridge.edutech.global.exception.ErrorCode;
import com.studybridge.edutech.global.exception.ErrorResponse;
import com.studybridge.edutech.global.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 인증은 되었지만 해당 API에 접근할 권한이 없는 요청에
 * 403 Forbidden을 공통 ErrorResponse(JSON) 형식으로 응답합니다.
 *
 * <p>예: ROLE_USER 사용자가 /api/v1/admin/** 요청</p>
 *
 * <p>인증 자체가 안 된 요청(토큰 없음)은 여기로 오지 않고
 * JsonAuthenticationEntryPoint(401)가 처리합니다.</p>
 *
 * <p>@Component로 등록하지 않고 SecurityConfig에서 직접 생성합니다.</p>
 */
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log =
            LoggerFactory.getLogger(JsonAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;

    public JsonAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        String traceId = UUID.randomUUID().toString();
        ErrorCode errorCode = ErrorCode.FORBIDDEN;

        /*
         * 권한 없는 접근 시도는 보안상 의미 있는 이벤트이므로
         * "누가" 시도했는지 userId를 함께 남기고, 레벨도 WARN으로 한다.
         */
        log.warn(
                "권한 없는 접근 시도. traceId={}, userId={}, method={}, uri={}",
                traceId,
                currentUserId(),
                request.getMethod(),
                request.getRequestURI()
        );

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
                response.getOutputStream(),
                ErrorResponse.of(errorCode, traceId)
        );
    }

    /**
     * JWT 필터가 SecurityContext에 저장한 AuthUser에서 userId를 꺼냅니다.
     */
    private String currentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.getPrincipal() instanceof AuthUser authUser) {
            return authUser.userId().toString();
        }

        return "unknown";
    }
}