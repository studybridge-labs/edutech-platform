package com.studybridge.edutech.identity.application.exception;

/**
 * 학생 프로필이 필요한 작업에서, 사용자에게 학생 프로필이 없을 때 발생하는 예외입니다.
 *
 * <p>요구사항상 ADMIN은 StudentProfile을 가지지 않을 수 있습니다.
 * 이런 계정이 학년 변경을 요청하면, 조용히 무시하지 않고 이 예외로 명확히 알립니다.
 * (무시하면 사용자는 학년이 바뀐 줄 알게 된다)</p>
 */
public class StudentProfileNotFoundException extends RuntimeException {

    public StudentProfileNotFoundException() {
        super("학생 프로필을 찾을 수 없습니다.");
    }
}