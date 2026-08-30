/**
 * Backend 로그인 API 성공 응답입니다.
 *
 * Refresh Token은 HttpOnly Cookie로 전달되므로
 * Response Body에는 포함되지 않습니다.
 */
export interface LoginResponse {
    accessToken: string;
    tokenType: "Bearer";
    expiresIn: number;
}