package com.studybridge.edutech.identity.domain;

/**
 * EduTech Platform 사용자의 권한을 정의합니다.
 *
 * <p>Spring Security의 인증 및 인가 과정에서 사용되며,
 * 사용자용 기능과 관리자용 기능의 접근 권한을 구분합니다.</p>
 */
public enum Role {

    /**
     * 일반 학생 사용자 권한입니다.
     */
    USER,

    /**
     * 학습 콘텐츠와 사용자 관리 기능에 접근할 수 있는 관리자 권한입니다.
     */
    ADMIN
}