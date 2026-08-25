import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useForm } from "react-hook-form";

import { signup } from "../api/signup";
import {
    signupSchema,
    type SignupFormValues,
} from "../schemas/signupSchema";
import type {
    ApiErrorResponse,
    SignupResponse,
} from "../types/signup";

/**
 * LOCAL 회원가입 입력 Form입니다.
 *
 * React Hook Form으로 Form 상태를 관리하고,
 * Zod로 클라이언트 입력값을 검증합니다.
 *
 * 검증에 성공하면 Backend 회원가입 API를 호출합니다.
 */
function SignupForm() {
    const [successMessage, setSuccessMessage] = useState("");
    const [serverError, setServerError] = useState("");

    const {
        register,
        handleSubmit,
        reset,
        setError,
        formState: {errors},
    } = useForm<SignupFormValues>({
        resolver: zodResolver(signupSchema),
        defaultValues: {
            email: "",
            password: "",
            nickname: "",
            grade: undefined,
        },
    });

    /**
     * 회원가입 API 요청 상태를 관리합니다.
     */
    const signupMutation = useMutation<
        SignupResponse,
        AxiosError<ApiErrorResponse>,
        SignupFormValues
    >({
        mutationFn: signup,

        onSuccess: (response) => {
            setServerError("");

            setSuccessMessage(
                `${response.nickname}님, 회원가입이 완료되었습니다.`,
            );

            reset();
        },

        onError: (error) => {
            setSuccessMessage("");

            const errorResponse = error.response?.data;

            /**
             * 중복 이메일은 이메일 입력창 아래에 표시합니다.
             */
            if (errorResponse?.code === "EMAIL_ALREADY_EXISTS") {
                setError("email", {
                    type: "server",
                    message: errorResponse.message,
                });

                return;
            }

            /**
             * 그 외 서버 오류는 Form 전체 오류로 표시합니다.
             */
            setServerError(
                errorResponse?.message ??
                "회원가입 중 오류가 발생했습니다.",
            );
        },
    });

    const onSubmit = (data: SignupFormValues) => {
        setSuccessMessage("");
        setServerError("");

        signupMutation.mutate(data);
    };

    return (
        <form
            className="signup-form"
            onSubmit={handleSubmit(onSubmit)}
            noValidate
        >
            <div className="form-group">
                <label className="form-label" htmlFor="email">
                    이메일
                </label>

                <input
                    className="form-input"
                    id="email"
                    type="email"
                    placeholder="student@example.com"
                    {...register("email")}
                />

                {errors.email && (
                    <p className="form-error" role="alert">
                        {errors.email.message}
                    </p>
                )}
            </div>

            <div className="form-group">
                <label className="form-label" htmlFor="password">
                    비밀번호
                </label>

                <input
                    className="form-input"
                    id="password"
                    type="password"
                    placeholder="8자 이상 입력해주세요."
                    {...register("password")}
                />

                {errors.password && (
                    <p className="form-error" role="alert">
                        {errors.password.message}
                    </p>
                )}
            </div>

            <div className="form-group">
                <label className="form-label" htmlFor="nickname">
                    닉네임
                </label>

                <input
                    className="form-input"
                    id="nickname"
                    type="text"
                    placeholder="사용할 닉네임을 입력해주세요."
                    {...register("nickname")}
                />

                {errors.nickname && (
                    <p className="form-error" role="alert">
                        {errors.nickname.message}
                    </p>
                )}
            </div>

            <div className="form-group">
                <label className="form-label" htmlFor="grade">
                    학년
                </label>

                <select
                    className="form-select"
                    id="grade"
                    defaultValue=""
                    {...register("grade")}
                >
                    <option value="" disabled>
                        학년을 선택해주세요.
                    </option>

                    <option value="MIDDLE_1">
                        중학교 1학년
                    </option>

                    <option value="MIDDLE_2">
                        중학교 2학년
                    </option>

                    <option value="MIDDLE_3">
                        중학교 3학년
                    </option>
                </select>

                {errors.grade && (
                    <p className="form-error" role="alert">
                        {errors.grade.message}
                    </p>
                )}
            </div>

            {serverError && (
                <p className="form-server-error" role="alert">
                    {serverError}
                </p>
            )}

            {successMessage && (
                <p className="form-success">
                    {successMessage}
                </p>
            )}

            <button
                className="signup-button"
                type="submit"
                disabled={signupMutation.isPending}
            >
                {signupMutation.isPending
                    ? "회원가입 중..."
                    : "회원가입"}
            </button>
        </form>
    );
}

export default SignupForm;