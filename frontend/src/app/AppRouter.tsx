import { Navigate, Route, Routes } from "react-router-dom";

import DashboardPage from "../pages/DashboardPage";
import LoginPage from "../pages/LoginPage";
import SignupPage from "../pages/SignupPage";
import ProtectedRoute from "./ProtectedRoute";

/**
 * 애플리케이션의 URL과 Page를 연결합니다.
 */
function AppRouter() {
    return (
        <Routes>
            {/* 인증 없이 접근 가능한 Page */}
            <Route
                path="/login"
                element={<LoginPage />}
            />

            <Route
                path="/signup"
                element={<SignupPage />}
            />

            {/* 인증된 사용자만 접근 가능한 Page */}
            <Route element={<ProtectedRoute />}>
                <Route
                    path="/dashboard"
                    element={<DashboardPage />}
                />
            </Route>

            {/* 기본 진입점 */}
            <Route
                path="/"
                element={
                    <Navigate
                        to="/login"
                        replace
                    />
                }
            />
        </Routes>
    );
}

export default AppRouter;