import axios from "axios";

/**
 * Backend REST API 호출에 사용하는 공통 Axios 인스턴스입니다.
 *
 * API 주소를 한 곳에서 관리하여
 * 각 기능에서 Backend URL을 직접 작성하지 않도록 합니다.
 */
export const httpClient = axios.create({
    baseURL:
        import.meta.env.VITE_API_BASE_URL ??
        "http://localhost:8080/api/v1",

    headers: {
        "Content-Type": "application/json",
    },
});