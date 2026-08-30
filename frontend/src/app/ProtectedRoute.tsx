import { Navigate, Outlet } from "react-router-dom";

import { useAuth } from "../features/auth/context/AuthContext";

/**
 * 인증된 사용자만 접근할 수 있는 Route입니다.
 */
function ProtectedRoute() {
    const {
        isAuthenticated,
        isInitialized,
    } = useAuth();

    /**
     * Refresh Token을 이용한 인증 복구가 끝날 때까지
     * 로그인 페이지로 보내지 않습니다.
     */
    if (!isInitialized) {
        return <div>로그인 상태를 확인하고 있습니다...</div>;
    }

    if (!isAuthenticated) {
        return (
            <Navigate
                to="/login"
                replace
            />
        );
    }

    return <Outlet />;
}

export default ProtectedRoute;