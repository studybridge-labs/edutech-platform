export type Grade =
    | "MIDDLE_1"
    | "MIDDLE_2"
    | "MIDDLE_3";

export interface SignupResponse {
    userId: string;
    email: string;
    nickname: string;
    grade: Grade;
}

/**
 * Backend의 공통 ErrorResponse 형식입니다.
 */
export interface ApiErrorResponse {
    status: number;
    code: string;
    message: string;
    traceId: string;
}