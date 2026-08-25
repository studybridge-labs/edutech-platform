package com.studybridge.edutech.identity.api.command.dto;

import com.studybridge.edutech.student.domain.Grade;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * LOCAL 회원가입 요청 정보를 전달하는 DTO입니다.
 *
 * <p>회원가입에 필요한 이메일, 비밀번호, 닉네임,
 * 학생의 현재 학년을 전달받습니다.</p>
 */
public record SignupRequest(

        /**
         * 로그인에 사용할 이메일입니다.
         */
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일은 255자를 초과할 수 없습니다.")
        String email,

        /**
         * LOCAL 로그인에 사용할 비밀번호입니다.
         *
         * <p>평문 비밀번호는 DB에 저장하지 않고
         * Application Service에서 BCrypt로 암호화합니다.</p>
         */
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(
                min = 8,
                max = 64,
                message = "비밀번호는 8자 이상 64자 이하로 입력해주세요."
        )
        String password,

        /**
         * 서비스에서 표시할 닉네임입니다.
         *
         * <p>닉네임 중복은 허용합니다.</p>
         */
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(
                min = 2,
                max = 50,
                message = "닉네임은 2자 이상 50자 이하로 입력해주세요."
        )
        String nickname,

        /**
         * 학생의 현재 학년입니다.
         */
        @NotNull(message = "학년은 필수입니다.")
        Grade grade
) {
}