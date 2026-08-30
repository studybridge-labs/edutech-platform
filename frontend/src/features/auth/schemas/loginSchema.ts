import { z } from "zod";

/**
 * 로그인 Form 입력값 검증 규칙입니다.
 */
export const loginSchema = z.object({
    email: z
        .string()
        .min(1, "이메일을 입력해주세요.")
        .email("올바른 이메일 형식이 아닙니다."),

    password: z
        .string()
        .min(1, "비밀번호를 입력해주세요."),
});

export type LoginFormValues =
    z.infer<typeof loginSchema>;