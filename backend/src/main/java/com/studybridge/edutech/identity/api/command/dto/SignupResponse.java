package com.studybridge.edutech.identity.api.command.dto;

import com.studybridge.edutech.identity.domain.User;
import com.studybridge.edutech.student.domain.Grade;
import com.studybridge.edutech.student.domain.StudentProfile;

import java.util.UUID;

/**
 * LOCAL 회원가입 성공 결과를 반환하는 DTO입니다.
 *
 * <p>비밀번호 Hash와 같은 인증 관련 민감정보는
 * 응답에 포함하지 않습니다.</p>
 */
public record SignupResponse(
        UUID userId,
        String email,
        String nickname,
        Grade grade
) {

    /**
     * 생성된 User와 StudentProfile을 회원가입 응답으로 변환합니다.
     *
     * @param user           생성된 사용자
     * @param studentProfile 생성된 학생 프로필
     * @return 회원가입 응답
     */
    public static SignupResponse from(
            User user,
            StudentProfile studentProfile
    ) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                studentProfile.getGrade()
        );
    }
}