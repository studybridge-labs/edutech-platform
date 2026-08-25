import SignupForm from "../features/auth/components/SignupForm";

/**
 * 회원가입 페이지입니다.
 */
function SignupPage() {
    return (
        <main>
            <h1>회원가입</h1>
            <p>StudyBridge 학습을 시작해보세요.</p>

            <SignupForm />
        </main>
    );
}

export default SignupPage;