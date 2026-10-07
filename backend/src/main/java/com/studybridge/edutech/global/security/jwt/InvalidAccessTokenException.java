package com.studybridge.edutech.global.security.jwt;

/**
 * Access Token이 유효하지 않을 때 발생하는 예외입니다.
 *
 * <p>토큰이 잘못되는 경우는 여러 가지다.</p>
 * <ul>
 *   <li>서명이 다름 (위조)</li>
 *   <li>만료됨</li>
 *   <li>형식이 깨짐</li>
 *   <li>Access Token이 아닌 다른 종류의 토큰</li>
 * </ul>
 *
 * <p>JJWT 라이브러리는 경우마다 다른 예외(ExpiredJwtException, SignatureException ...)를 던진다.
 * 이를 이 예외 하나로 감싸서, 사용하는 쪽(JWT 필터)은
 * "유효한가 / 아닌가"만 신경 쓰도록 한다.
 * → 필터가 JJWT 라이브러리 내부 사정을 몰라도 된다.</p>
 */
public class InvalidAccessTokenException extends RuntimeException {

    public InvalidAccessTokenException(String message) {
        super(message);
    }

    /**
     * @param cause 원래 발생한 JJWT 예외. 로그에서 실제 원인을 확인할 수 있도록 보관한다.
     */
    public InvalidAccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}