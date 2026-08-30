import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";

import { login } from "../api/login";
import {
    loginSchema,
    type LoginFormValues,
} from "../schemas/loginSchema";
import { useAuth } from "../context/AuthContext";
import type { ApiErrorResponse } from "../types/signup";

/**
 * LOCAL 로그인 Form입니다.
 *
 * React Hook Form으로 입력 상태를 관리하고,
 * Zod로 클라이언트 Validation을 수행합니다.
 *
 * 로그인에 성공하면 Access Token을 메모리에 저장하고
 * Dashboard 페이지로 이동합니다.
 */
function LoginForm() {
    const [serverError, setServerError] = useState("");

    const navigate = useNavigate();
    const { login: authenticate } = useAuth();

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: "",
            password: "",
        },
    });

    /**
     * 로그인 API 요청 상태를 관리합니다.
     */
    const loginMutation = useMutation({
        mutationFn: login,

        onSuccess: (response) => {
            setServerError("");

            /**
             * Access Token은 localStorage가 아니라
             * JavaScript 메모리에만 저장합니다.
             */
            authenticate(response.accessToken);

            /**
             * 로그인에 성공하면
             * 인증 사용자 전용 Dashboard로 이동합니다.
             */
            navigate("/dashboard");
        },

        onError: (
            error: AxiosError<ApiErrorResponse>,
        ) => {
            const errorResponse =
                error.response?.data;

            setServerError(
                errorResponse?.message ??
                "로그인 중 오류가 발생했습니다.",
            );
        },
    });

    const onSubmit = (data: LoginFormValues) => {
        setServerError("");

        loginMutation.mutate(data);
    };

    return (
        <form
            className="signup-form"
            onSubmit={handleSubmit(onSubmit)}
            noValidate
        >
            <div className="form-group">
                <label
                    className="form-label"
                    htmlFor="email"
                >
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
                    <p
                        className="form-error"
                        role="alert"
                    >
                        {errors.email.message}
                    </p>
                )}
            </div>

            <div className="form-group">
                <label
                    className="form-label"
                    htmlFor="password"
                >
                    비밀번호
                </label>

                <input
                    className="form-input"
                    id="password"
                    type="password"
                    placeholder="비밀번호를 입력해주세요."
                    {...register("password")}
                />

                {errors.password && (
                    <p
                        className="form-error"
                        role="alert"
                    >
                        {errors.password.message}
                    </p>
                )}
            </div>

            {serverError && (
                <p
                    className="form-server-error"
                    role="alert"
                >
                    {serverError}
                </p>
            )}

            <button
                className="signup-button"
                type="submit"
                disabled={loginMutation.isPending}
            >
                {loginMutation.isPending
                    ? "로그인 중..."
                    : "로그인"}
            </button>
        </form>
    );
}

export default LoginForm;