import { httpClient } from "../../../api/httpClient";

import type { LoginResponse } from "../types/login";

/**
 * HttpOnly Cookie에 저장된 Refresh Token을 이용하여
 * 새로운 Access Token을 발급받습니다.
 *
 * Refresh Token 자체는 JavaScript에서 읽지 않습니다.
 */
export async function refreshAccessToken(): Promise<LoginResponse> {
    const response =
        await httpClient.post<LoginResponse>(
            "/auth/refresh",
        );

    return response.data;
}