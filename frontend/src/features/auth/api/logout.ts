import { httpClient } from "../../../api/httpClient";

/**
 * 현재 로그인 세션을 종료합니다.
 *
 * Refresh Token은 HttpOnly Cookie로 자동 전달되며,
 * Backend에서 AuthSession을 revoke하고 Cookie를 삭제합니다.
 */
export async function logoutRequest(): Promise<void> {
    await httpClient.post("/auth/logout");
}