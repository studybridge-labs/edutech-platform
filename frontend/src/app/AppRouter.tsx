import { Navigate, Route, Routes } from "react-router-dom";

import SignupPage from "../pages/SignupPage";

/**
 * 애플리케이션의 URL과 페이지를 연결합니다.
 */
function AppRouter() {
    return (
        <Routes>
            <Route path="/signup" element={<SignupPage />} />

    {/* 아직 홈 화면이 없으므로 임시로 회원가입 페이지로 이동합니다. */}
    <Route path="/" element={<Navigate to="/signup" replace />} />
    </Routes>
);
}

export default AppRouter;