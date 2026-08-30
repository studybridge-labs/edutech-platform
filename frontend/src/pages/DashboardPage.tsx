import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../features/auth/context/AuthContext";

/**
 * 로그인한 사용자가 접근하는 학습 Dashboard입니다.
 *
 * 현재는 인증 흐름 확인을 위한 기본 화면이며,
 * 이후 학습 현황과 추천 문제 등을 추가합니다.
 */
function DashboardPage() {
    const navigate = useNavigate();

    const { logout } = useAuth();

    const [isLoggingOut, setIsLoggingOut] =
        useState(false);

    const [logoutError, setLogoutError] =
        useState("");

    const handleLogout = async () => {
        setIsLoggingOut(true);
        setLogoutError("");

        try {
            /**
             * Backend AuthSession을 revoke하고
             * Refresh Token Cookie를 삭제합니다.
             */
            await logout();

            /**
             * 로그아웃 성공 후 로그인 페이지로 이동합니다.
             */
            navigate("/login", {
                replace: true,
            });
        } catch {
            setLogoutError(
                "로그아웃 처리 중 오류가 발생했습니다.",
            );
        } finally {
            setIsLoggingOut(false);
        }
    };

    return (
        <main
            style={{
                padding: "40px",
            }}
        >
            <h1>학습 대시보드</h1>

            <p>
                StudyBridge에 오신 것을 환영합니다.
            </p>

            <button
                type="button"
                onClick={handleLogout}
                disabled={isLoggingOut}
            >
                {isLoggingOut
                    ? "로그아웃 중..."
                    : "로그아웃"}
            </button>

            {logoutError && (
                <p role="alert">
                    {logoutError}
                </p>
            )}
        </main>
    );
}

export default DashboardPage;