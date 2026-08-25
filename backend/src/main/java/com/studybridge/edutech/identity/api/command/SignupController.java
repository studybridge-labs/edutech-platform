package com.studybridge.edutech.identity.api.command;

import com.studybridge.edutech.identity.api.command.dto.SignupRequest;
import com.studybridge.edutech.identity.api.command.dto.SignupResponse;
import com.studybridge.edutech.identity.application.command.SignupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * LOCAL 회원가입 API를 제공합니다.
 *
 * <p>HTTP 요청을 검증한 뒤 회원가입 Use Case를 Application 계층에 위임합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    /**
     * 새로운 LOCAL 사용자 계정을 생성합니다.
     *
     * @param request 회원가입 요청
     * @return 생성된 사용자 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = signupService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}