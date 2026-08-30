import { httpClient } from "../../../api/httpClient";

import type { LoginFormValues } from "../schemas/loginSchema";
import type { LoginResponse } from "../types/login";

/**
 * 이메일과 비밀번호를 이용하여 로그인합니다.
 *
 * Backend는 Access Token을 Response Body로,
 * Refresh Token을 HttpOnly Cookie로 반환합니다.
 */
export async function login(
    request: LoginFormValues,
): Promise<LoginResponse> {
    const response =
        await httpClient.post<LoginResponse>(
            "/auth/login",
            request,
        );

    return response.data;
}