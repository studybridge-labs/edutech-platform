import SignupForm from "../features/auth/components/SignupForm";
import "../features/auth/components/SignupForm.css";

/**
 * 회원가입 페이지입니다.
 */
function SignupPage() {
    return (
        <main className="signup-page">
            <section className="signup-card">
                <header className="signup-header">
                    <div className="signup-logo">
                        STUDYBRIDGE
                    </div>

                    <h1 className="signup-title">
                        회원가입
                    </h1>

                    <p className="signup-description">
                        나에게 맞는 영어·수학 학습을 시작해보세요.
                    </p>
                </header>

                <SignupForm />

                <div className="signup-footer">
                    이미 계정이 있으신가요?{" "}
                    <a href="/login">로그인</a>
                </div>
            </section>
        </main>
    );
}

export default SignupPage;