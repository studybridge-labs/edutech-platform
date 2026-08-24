package com.studybridge.edutech.identity.domain;

/**
 * 사용자 계정의 현재 상태를 정의합니다.
 *
 * <p>회원 탈퇴나 정지 상태를 물리적으로 즉시 삭제하지 않고
 * 계정 상태를 기준으로 서비스 접근 가능 여부를 판단하기 위해 사용합니다.</p>
 */
public enum UserStatus {

    /**
     * 정상적으로 서비스를 이용할 수 있는 계정입니다.
     */
    ACTIVE,

    /**
     * 관리자 정책 등에 의해 일시적으로 이용이 제한된 계정입니다.
     */
    SUSPENDED,

    /**
     * 회원 탈퇴가 처리된 계정입니다.
     */
    WITHDRAWN
}