import { httpClient } from "../../../api/httpClient";

import type { SignupFormValues } from "../schemas/signupSchema";
import type { SignupResponse } from "../types/signup";

/**
 * LOCAL 회원가입 API를 호출합니다.
 *
 * @param request 회원가입 입력값
 * @returns 생성된 사용자 정보
 */
export async function signup(
    request: SignupFormValues,
): Promise<SignupResponse> {
    const response = await httpClient.post<SignupResponse>(
        "/auth/signup",
        request,
    );

    return response.data;
}