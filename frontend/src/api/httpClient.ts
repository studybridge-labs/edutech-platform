import axios from "axios";

import { getAccessToken } from "../features/auth/token/accessTokenStore";

/**
 * Backend REST API 호출에 사용하는 공통 Axios 인스턴스입니다.
 */
export const httpClient = axios.create({
    baseURL:
        import.meta.env.VITE_API_BASE_URL ??
        "http://localhost:8080/api/v1",

    headers: {
        "Content-Type": "application/json",
    },

    /**
     * Refresh Token HttpOnly Cookie를
     * Frontend와 Backend 사이에서 전달하기 위해 필요합니다.
     */
    withCredentials: true,
});

/**
 * Access Token이 존재하면 모든 API 요청에
 * Authorization Header를 자동으로 추가합니다.
 */
httpClient.interceptors.request.use((config) => {
    const accessToken = getAccessToken();

    if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
});