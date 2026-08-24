# EduTech Platform - Requirements

## 1. 프로젝트 개요

중학생을 대상으로 영어와 수학 학습을 제공하는 맞춤형 EduTech 플랫폼을 개발한다.

서비스 핵심 흐름은 다음과 같다.

```text
회원가입 / 로그인
        ↓
학생 학년 설정
        ↓
영어 / 수학 선택
        ↓
진단평가
        ↓
문제 풀이
        ↓
자동 채점
        ↓
학습 기록
        ↓
오답 분석
        ↓
취약 단원 분석
        ↓
맞춤 문제 추천
        ↓
학습 대시보드
```

단순한 문제은행이 아니라 학생의 학습 데이터를 누적하고 분석하여
다음 학습을 제안하는 서비스를 목표로 한다.

---

# 2. 서비스 대상

## 학생

V1의 주요 사용자는 중학생이다.

지원 학년:

```text
MIDDLE_1
MIDDLE_2
MIDDLE_3
```

지원 과목:

```text
MATH
ENGLISH
```

## 관리자

관리자는 학습 콘텐츠와 사용자를 관리한다.

```text
과목 관리
단원 관리
독해 지문 관리
문제 관리
사용자 조회
콘텐츠 활성 / 비활성 관리
```

---

# 3. 사용자 권한

```text
USER
ADMIN
```

## USER

학생 사용자 권한이다.

```text
회원가입
로그인
로그아웃
프로필 관리
진단평가
문제 풀이
자동 채점
학습 기록
오답노트
취약 단원
추천 문제
대시보드
```

## ADMIN

```text
과목 관리
단원 관리
지문 관리
문제 등록
문제 수정
문제 비활성화
사용자 조회
```

일반 사용자가 ADMIN API에 접근하면:

```text
403 Forbidden
```

---

# 4. 회원가입

일반 회원가입 정보:

```text
이메일
비밀번호
닉네임
학년
```

검증:

```text
이메일 형식
이메일 중복
비밀번호 정책
닉네임 길이
학년 값
```

이메일:

```text
UNIQUE
```

닉네임:

```text
중복 허용
```

닉네임은 사용자 고유 식별자로 사용하지 않는다.

비밀번호:

```text
Raw Password
    ↓
BCrypt
    ↓
Password Hash
```

---

# 5. 인증 방식

지원 로그인:

```text
LOCAL
GOOGLE
KAKAO
NAVER
```

## LOCAL

```text
Email + Password
```

## Social Login

```text
OAuth Provider 인증
        ↓
provider + providerUserId 확인
        ↓
SocialAccount 조회
        ↓
기존 사용자?
        ├── YES → 로그인
        └── NO  → 신규 회원 Onboarding
```

---

# 6. 소셜 계정 연결

동일한 이메일이라는 이유만으로 계정을 자동 병합하지 않는다.

```text
LOCAL student@gmail.com

GOOGLE student@gmail.com

→ 자동 병합 X
```

사용자가 인증된 상태에서 명시적인 Social Account Linking을 수행한다.

하나의 User에는:

```text
Google 1개
Kakao 1개
Naver 1개
```

까지 연결할 수 있다.

---

# 7. 학생 프로필

학생은 별도의 StudentProfile을 가진다.

```text
User
 │
 └── StudentProfile
        └── Grade
```

Grade:

```text
MIDDLE_1
MIDDLE_2
MIDDLE_3
```

ADMIN은 StudentProfile을 가지지 않을 수 있다.

---

# 8. 과목

V1:

```text
MATH
ENGLISH
```

---

# 9. 수학 학습

지원 유형:

```text
개념 문제
계산 문제
유형 문제
응용 문제
```

문제 형태:

```text
MULTIPLE_CHOICE
SHORT_ANSWER
```

---

# 10. 영어 학습

## Vocabulary

```text
영단어 → 뜻 선택
빈칸 → 단어 선택
```

## Grammar

```text
올바른 문장 선택
빈칸 표현 선택
```

## Reading

```text
주제 찾기
내용 일치
빈칸 추론
```

V1에서는 Listening을 제외한다.

---

# 11. 영어 독해 지문

하나의 Reading Passage에 여러 문제를 연결할 수 있다.

```text
Passage
 │
 ├── Question 1
 ├── Question 2
 └── Question 3
```

Passage 정보:

```text
단원
제목
본문
활성 상태
```

Reading 문제가 아닌 Question은 Passage를 가지지 않을 수 있다.

---

# 12. 단원

Unit은 계층 구조를 지원한다.

예:

```text
영어
└── Grammar
    ├── 시제
    └── 조동사
```

```text
수학
└── 중2
    ├── 식의 계산
    ├── 연립방정식
    └── 일차함수
```

---

# 13. 문제 유형

V1:

```text
MULTIPLE_CHOICE
SHORT_ANSWER
```

향후:

```text
TRUE_FALSE
ESSAY
LISTENING
```

---

# 14. 문제 난이도

```text
EASY
MEDIUM
HARD
```

추천 알고리즘에 사용한다.

---

# 15. 학습 콘텐츠 표현

문제와 Passage는 다음 콘텐츠 표현을 지원한다.

```text
Markdown
+
LaTeX
```

예:

```markdown
다음 방정식의 해를 구하세요.

$$
x^2 + 3x - 4 = 0
$$
```

Frontend에서는 Markdown 및 수식 Renderer를 사용한다.

HTML을 직접 렌더링하는 경우 Sanitization을 적용한다.

---

# 16. 문제 조회

학생은 다음 조건으로 문제를 조회할 수 있다.

```text
과목
학년
단원
난이도
문제 유형
```

학생용 문제 Response에는 다음 정보를 노출하지 않는다.

```text
정답
isCorrect
correctAnswer
explanation
```

정답 판정은 Backend에서 수행한다.

---

# 17. 문제 제출

처리 흐름:

```text
사용자 인증
    ↓
문제 확인
    ↓
답안 Validation
    ↓
정답 판정
    ↓
QuestionAttempt 생성
    ↓
저장
    ↓
결과 반환
```

QuestionAttempt 저장 정보:

```text
User
Question
submissionId

selectedOptionId
또는
answerText

correct
solvingTimeMs
attemptType
attemptedAt
```

객관식:

```text
selectedOptionId = 값
answerText = NULL
```

단답형:

```text
selectedOptionId = NULL
answerText = 값
```

---

# 18. 중복 제출 방지

답안 제출마다 Client가 UUID 기반 `submissionId`를 생성한다.

```text
Client
    ↓
submissionId
    ↓
Backend
```

동일한 submissionId가 다시 들어오면
새로운 QuestionAttempt를 생성하지 않는다.

방지 대상:

```text
Double Click
Network Retry
Frontend Retry
```

---

# 19. 문제 풀이 유형

```text
NORMAL
DIAGNOSTIC
REVIEW
```

NORMAL:

```text
일반 학습
```

DIAGNOSTIC:

```text
진단평가
```

REVIEW:

```text
오답 복습
```

---

# 20. 자동 채점

정답 판단은 Backend에서 수행한다.

일반 학습 문제는 제출 후:

```text
정답 여부
정답
해설
```

을 반환할 수 있다.

진단평가에서는 평가 진행 중 정답과 해설을 공개하지 않는다.

---

# 21. 학습 기록

모든 문제 제출은 학습 기록으로 남긴다.

같은 문제를 여러 번 풀어도 기존 기록을 수정하지 않는다.

```text
1회 오답
2회 오답
3회 정답

→ 3개의 QuestionAttempt 유지
```

QuestionAttempt는 학습 History로 취급한다.

---

# 22. 풀이 시간

`solvingTimeMs`는 Frontend가 전달하는 참고 데이터이다.

따라서 조작 가능성을 전제로 한다.

```text
학습 분석 참고
→ 가능

시험 보안 판정
→ 사용 X

사용자 인증
→ 사용 X

랭킹
→ 사용 X
```

Backend는 음수나 비정상적인 범위의 값을 Validation한다.

---

# 23. 오답노트

V1에서는 별도 WrongAnswer 테이블을 만들지 않는다.

```text
QuestionAttempt
WHERE correct = false
```

를 기반으로 계산한다.

복습 후 정답을 맞혀도 과거 오답 기록은 삭제하지 않는다.

---

# 24. 진단평가

학생은 영어 또는 수학 진단평가를 진행한다.

```text
진단평가 시작
        ↓
10~15개 문제 선정
        ↓
문제 풀이
        ↓
자동 채점
        ↓
평가 완료
        ↓
단원별 정답률
        ↓
취약 단원
```

상태:

```text
IN_PROGRESS
COMPLETED
ABANDONED
```

---

# 25. 진단평가 문제 고정

진단평가가 시작되면 해당 Session에서 사용할 문제를 고정한다.

```text
DiagnosticSession
        ↓
DiagnosticSessionQuestion
        ↓
Question
```

평가 시작 이후 Question이 추가되거나 변경되어도
기존 Session의 출제 문제 목록은 변경하지 않는다.

한 DiagnosticSession에서 같은 Question은 한 번만 제출할 수 있다.

---

# 26. 취약 단원

단원별 정답률을 계산한다.

예:

```text
연립방정식 40%
일차함수   55%
확률       85%
```

낮은 정답률의 Unit을 취약 단원으로 판단한다.

---

# 27. 문제 추천

V1은 규칙 기반 추천을 사용한다.

```text
정답률 < 50%
→ EASY

50% <= 정답률 < 80%
→ MEDIUM

정답률 >= 80%
→ HARD
```

AI 기반 추천은 V2 이후 검토한다.

---

# 28. Dashboard

```text
오늘 풀이 수
오늘 정답 수
오늘 오답 수
오늘 정답률

누적 문제 수

과목별 정답률
단원별 정답률

최근 학습
취약 단원
추천 학습
```

V1에서는 QuestionAttempt를 기반으로 Query한다.

---

# 29. 관리자 문제 관리

관리자는 다음 정보를 입력하여 문제를 생성한다.

```text
단원
Passage 선택
문제 유형
난이도
문제 내용
선택지
정답
해설
```

## 문제 수정

QuestionAttempt가 아직 없는 문제:

```text
수정 가능
```

QuestionAttempt가 존재하는 문제:

```text
문제 내용
정답
선택지
핵심 채점 기준

→ 변경 제한
```

문제 오류가 발견되면:

```text
기존 Question
→ active = false

새 Question
→ 생성
```

향후 Question Versioning을 검토할 수 있다.

---

# 30. Logical CQRS

V1에서는 Logical CQRS를 적용한다.

Command:

```text
회원가입
로그인
로그아웃
Token Refresh
프로필 변경
진단평가 시작
답안 제출
진단평가 완료
관리자 콘텐츠 등록/수정/비활성화
```

Query:

```text
프로필
과목
단원
문제
진단 결과
학습 기록
오답
취약 단원
추천
Dashboard
```

DB는 하나의 PostgreSQL을 사용한다.

---

# 31. 향후 CQRS 확장

```text
Command
    ↓
PostgreSQL
    ↓
Domain Event
    ↓
Transactional Outbox
    ↓
Kafka
    ↓
Projection
    ↓
Redis / Read DB
    ↓
Query
```

Kafka와 Redis는 V1 필수 기술이 아니다.

---

# 32. 보안

필수:

```text
Spring Security
BCrypt
OAuth2
JWT
Access Token
Refresh Token
USER / ADMIN Authorization
Ownership Validation
HTTPS
CORS
Validation
Secret 관리
```

다른 학생의 데이터에 접근할 수 없어야 한다.

---

# 33. 테스트

```text
JUnit 5
Mockito
AssertJ
MockMvc
Repository Test
Security Test
Integration Test
```

필수 Security 시나리오:

```text
비로그인 → 보호 API → 401

USER → ADMIN API → 403

Student A → Student B Resource → 거부

잘못된 Token → 401

만료 Token → 401

중복 submissionId
→ QuestionAttempt 1건만 생성
```

---

# 34. V1 완료 기준

```text
회원가입 / OAuth Login
        ↓
학년 설정
        ↓
영어 / 수학
        ↓
진단평가
        ↓
결과
        ↓
추천 문제
        ↓
일반 학습
        ↓
자동 채점
        ↓
오답 복습
        ↓
Dashboard
        ↓
로그아웃
```

---

# 35. V1 제외

```text
실시간 채팅
화상 강의
실시간 수업
학부모 계정
교사 계정
결제
랭킹
커뮤니티
스터디 그룹
AI 문제 생성
AI 튜터
영어 듣기
```