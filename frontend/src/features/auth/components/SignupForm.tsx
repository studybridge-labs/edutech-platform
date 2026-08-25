import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import {
    signupSchema,
    type SignupFormValues,
} from "../schemas/signupSchema";

/**
 * LOCAL 회원가입 입력 Form입니다.
 *
 * React Hook Form으로 Form 상태를 관리하고,
 * Zod를 통해 클라이언트 입력값을 검증합니다.
 */
function SignupForm() {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<SignupFormValues>({
        resolver: zodResolver(signupSchema),
        defaultValues: {
            email: "",
            password: "",
            nickname: "",
            grade: undefined,
        },
    });

    const onSubmit = (data: SignupFormValues) => {
        // 다음 단계에서 회원가입 API를 연결합니다.
        console.log("회원가입 요청:", data);
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <div>
                <label htmlFor="email">이메일</label>

                <input
                    id="email"
                    type="email"
                    placeholder="student@example.com"
                    {...register("email")}
                />

                {errors.email && <p>{errors.email.message}</p>}
            </div>

            <div>
                <label htmlFor="password">비밀번호</label>

                <input
                    id="password"
                    type="password"
                    placeholder="비밀번호를 입력해주세요."
                    {...register("password")}
                />

                {errors.password && <p>{errors.password.message}</p>}
            </div>

            <div>
                <label htmlFor="nickname">닉네임</label>

                <input
                    id="nickname"
                    type="text"
                    placeholder="닉네임을 입력해주세요."
                    {...register("nickname")}
                />

                {errors.nickname && <p>{errors.nickname.message}</p>}
            </div>

            <div>
                <label htmlFor="grade">학년</label>

                <select id="grade" defaultValue="" {...register("grade")}>
                    <option value="" disabled>
                        학년을 선택해주세요.
                    </option>
                    <option value="MIDDLE_1">중학교 1학년</option>
                    <option value="MIDDLE_2">중학교 2학년</option>
                    <option value="MIDDLE_3">중학교 3학년</option>
                </select>

                {errors.grade && <p>{errors.grade.message}</p>}
            </div>

            <button type="submit">회원가입</button>
        </form>
    );
}

export default SignupForm;