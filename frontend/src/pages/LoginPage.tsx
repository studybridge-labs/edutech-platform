import LoginForm from "../features/auth/components/LoginForm";
import "../features/auth/components/SignupForm.css";

/**
 * 로그인 페이지입니다.
 */
function LoginPage() {
    return (
        <main className="signup-page">
            <section className="signup-card">
                <header className="signup-header">
                    <div className="signup-logo">
                        STUDYBRIDGE
                    </div>

                    <h1 className="signup-title">
                        로그인
                    </h1>

                    <p className="signup-description">
                        학습을 계속 이어가세요.
                    </p>
                </header>

                <LoginForm />

                <div className="signup-footer">
                    아직 계정이 없으신가요?{" "}
                    <a href="/signup">
                        회원가입
                    </a>
                </div>
            </section>
        </main>
    );
}

export default LoginPage;