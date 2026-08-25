import { z } from "zod";

/**
 * 회원가입 Form에서 사용하는 입력값 검증 규칙입니다.
 *
 * Backend Validation과 최대한 동일한 기준을 사용하여
 * 서버 요청 전에 잘못된 입력을 먼저 차단합니다.
 */
export const signupSchema = z.object({
    email: z
        .string()
        .min(1, "이메일을 입력해주세요.")
        .email("올바른 이메일 형식이 아닙니다.")
        .max(255, "이메일은 255자 이하로 입력해주세요."),

    password: z
        .string()
        .min(8, "비밀번호는 8자 이상 입력해주세요.")
        .max(64, "비밀번호는 64자 이하로 입력해주세요."),

    nickname: z
        .string()
        .min(2, "닉네임은 2자 이상 입력해주세요.")
        .max(50, "닉네임은 50자 이하로 입력해주세요."),

    grade: z.enum(["MIDDLE_1", "MIDDLE_2", "MIDDLE_3"], {
        message: "학년을 선택해주세요.",
    }),
});

export type SignupFormValues = z.infer<typeof signupSchema>;