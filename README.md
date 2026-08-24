# 📚 StudyBridge EduTech Platform

> 중학생을 위한 영어·수학 맞춤형 학습 플랫폼

StudyBridge EduTech Platform은 단순히 문제를 제공하는 문제은행이 아니라,  
학생의 문제 풀이 데이터를 지속적으로 기록하고 분석하여 **진단평가 → 취약 단원 분석 → 맞춤 문제 추천 → 오답 복습**으로 이어지는 학습 경험을 제공하는 것을 목표로 합니다.

---

## 🎯 Project Goal

학생의 학습 과정은 다음과 같이 연결됩니다.

```text
회원가입 / 로그인
        ↓
학년 설정
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

V1에서는 **중학교 1~3학년 영어와 수학**을 대상으로 구현합니다.

---

# ✨ Core Features

## 👤 회원 / 인증

```text
LOCAL 회원가입 / 로그인

Google OAuth

Kakao OAuth

Naver OAuth

Access Token

Refresh Token

USER / ADMIN 권한
```

동일한 이메일이라는 이유만으로 Social Account를 자동 병합하지 않으며,
사용자의 명시적인 인증 과정을 거쳐 계정을 연결합니다.

---

## 🧑‍🎓 학생 프로필

학생은 자신의 학년 정보를 관리할 수 있습니다.

```text
MIDDLE_1

MIDDLE_2

MIDDLE_3
```

학년에 따라 영어·수학 학습 콘텐츠와 진단평가가 제공됩니다.

---

## ➗ Mathematics

수학은 다음 학습 유형을 지원합니다.

```text
개념 문제

계산 문제

유형 문제

응용 문제
```

문제 유형:

```text
MULTIPLE_CHOICE

SHORT_ANSWER
```

수학식 표현을 위해 Markdown과 LaTeX를 지원할 수 있도록 설계합니다.

예:

```text
x² + 3x - 4 = 0
```

---

## 🔤 English

영어는 다음 영역을 지원합니다.

```text
Vocabulary

Grammar

Reading
```

Reading의 경우 하나의 지문에 여러 문제를 연결할 수 있습니다.

```text
Passage
   │
   ├── Question 1
   ├── Question 2
   └── Question 3
```

Listening은 V2 이후 확장을 고려합니다.

---

# 📝 Diagnostic Test

학생은 영어 또는 수학 진단평가를 진행할 수 있습니다.

```text
진단평가 시작
        ↓
10 ~ 15 문제 출제
        ↓
문제 풀이
        ↓
자동 채점
        ↓
평가 완료
        ↓
단원별 정답률
        ↓
취약 단원 분석
```

진단평가가 시작되면 출제 문제는 Session 단위로 고정됩니다.

```text
DiagnosticSession
        ↓
DiagnosticSessionQuestion
        ↓
Question
```

진단평가 도중에는 정답과 해설을 공개하지 않고,
평가 완료 후 결과를 제공합니다.

---

# ✅ Automatic Grading

학생의 정답 여부는 Frontend가 아닌 Backend에서 판정합니다.

```text
Student Answer
      ↓
Backend
      ↓
Question Answer
      ↓
Correctness Check
      ↓
QuestionAttempt
```

일반 문제에서는 제출 후 다음 정보를 제공할 수 있습니다.

```text
정답 여부

정답

해설
```

---

# 📊 Learning History

학생이 문제를 제출할 때마다 `QuestionAttempt`를 생성합니다.

같은 문제를 여러 번 풀어도 기존 기록을 덮어쓰지 않습니다.

```text
1회
오답

2회
오답

3회
정답
```

위 경우 총 3개의 학습 기록을 유지합니다.

이를 기반으로:

```text
오답노트

과목별 정답률

단원별 정답률

취약 단원

학습 진도

추천 문제

Dashboard
```

를 조회합니다.

---

# ❌ Wrong Answer Review

V1에서는 별도의 `wrong_answers` 테이블을 만들지 않습니다.

```text
QuestionAttempt
        ↓
correct = false
        ↓
Wrong Answer Query
```

오답을 다시 맞혀도 기존 오답 이력은 유지합니다.

---

# 🎯 Recommendation

V1에서는 복잡한 AI 추천보다 규칙 기반 추천부터 구현합니다.

```text
정답률 < 50%
→ EASY

50% <= 정답률 < 80%
→ MEDIUM

정답률 >= 80%
→ HARD
```

서비스 데이터가 충분히 확보된 이후 추천 알고리즘과 AI 기능을 확장할 수 있도록 설계합니다.

---

# 🧠 Architecture

V1의 핵심 아키텍처는 다음과 같습니다.

```text
Modular Monolith

+

Domain-Oriented Architecture

+

Logical CQRS

+

PostgreSQL
```

전체 구조:

```text
┌─────────────────────────────┐
│     React + TypeScript      │
└─────────────┬───────────────┘
              │
              │ HTTPS / REST
              ▼
┌─────────────────────────────┐
│        Spring Boot          │
│                             │
│  Identity                   │
│  Student                    │
│  Curriculum                 │
│  Learning                   │
│  Analytics                  │
│                             │
│  Command / Query            │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│         PostgreSQL          │
└─────────────────────────────┘
```

V1에서는 Kafka, Redis, Read DB / Write DB 분리를 필수로 사용하지 않습니다.

---

# 🔀 CQRS

상태 변경과 조회의 책임을 코드 레벨에서 분리합니다.

## Command

```text
회원가입

로그인

로그아웃

프로필 변경

진단평가 시작

답안 제출

진단평가 완료

관리자 콘텐츠 관리
```

흐름:

```text
Command Controller
        ↓
Command Service
        ↓
Domain
        ↓
Repository
        ↓
PostgreSQL
```

## Query

```text
문제 조회

진단평가 결과

오답노트

취약 단원

추천 문제

Dashboard
```

흐름:

```text
Query Controller
        ↓
Query Service
        ↓
Query Repository
        ↓
Projection / Aggregate
        ↓
PostgreSQL
```

Command와 Query는 V1에서 동일한 PostgreSQL을 사용합니다.

---

# 🧩 Domain Structure

```text
Identity
├── User
├── SocialAccount
└── AuthSession

Student
└── StudentProfile

Curriculum
├── Subject
├── Unit
├── Passage
├── Question
└── QuestionOption

Learning
├── DiagnosticSession
├── DiagnosticSessionQuestion
└── QuestionAttempt

Analytics
├── Dashboard
├── Wrong Answer
├── Progress
├── Weak Unit
└── Recommendation
```

---

# 🗄 Database

V1 핵심 테이블:

```text
1. users
2. student_profiles
3. social_accounts
4. auth_sessions

5. subjects
6. units
7. passages
8. questions
9. question_options

10. diagnostic_sessions
11. diagnostic_session_questions
12. question_attempts
```

Primary Key:

```text
UUID
```

Schema Migration:

```text
Flyway
```

Production에서는 `ddl-auto=update`에 의존하지 않고
Migration 기반으로 Schema를 관리하는 것을 목표로 합니다.

---

# 🔐 Security

다음 보안 원칙을 기본 설계에 포함합니다.

```text
Spring Security

BCrypt Password Hashing

JWT

Access Token

Refresh Token Rotation

HttpOnly Refresh Cookie

OAuth2

USER / ADMIN Authorization

Resource Ownership Validation

CORS

CSRF 고려

HTTPS

Input Validation

Rate Limiting

Secret Management
```

특히 학생 데이터는 URL에서 전달된 User ID를 신뢰하지 않고:

```text
/api/v1/me/**
```

형태의 API를 우선 사용합니다.

또한 UUID 사용 여부와 관계없이
모든 개인 Resource에 대해 Ownership 검증을 수행합니다.

---

# 🛡 Answer Protection

학생용 문제 조회 API에서는 다음 데이터를 노출하지 않습니다.

```text
isCorrect

correctAnswer

explanation
```

정답 판정은 서버에서 수행합니다.

진단평가 진행 중에는 답안을 제출해도:

```text
정답 여부

정답

해설
```

을 공개하지 않습니다.

---

# 🔁 Duplicate Submission Protection

답안 제출 시 Client에서 `submissionId`를 생성합니다.

```text
UUID submissionId
       ↓
Backend
       ↓
QuestionAttempt
```

동일한 submissionId가 재요청되면 새로운 학습 기록을 생성하지 않습니다.

이를 통해:

```text
Double Click

Network Retry

Frontend Retry
```

등에 의한 중복 저장을 방지합니다.

---

# 🛠 Tech Stack

## Backend

```text
Java

Spring Boot

Spring Security

Spring Data JPA

Spring Validation

OAuth2 Client

PostgreSQL

Flyway

Gradle
```

## Frontend

```text
React

TypeScript

Vite

React Router

TanStack Query

React Hook Form

Zod
```

## Test

```text
JUnit 5

Mockito

AssertJ

MockMvc
```

## Infrastructure

```text
Docker

GitHub Actions

HTTPS

Managed PostgreSQL
```

세부 배포 플랫폼은 실제 개발 단계에서 확정합니다.

---

# 📁 Project Structure

```text
edutech-platform
│
├── backend
│
├── frontend
│
├── docs
│   ├── requirements.md
│   ├── architecture.md
│   ├── erd.md
│   ├── api-spec.md
│   └── security.md
│
├── .gitignore
└── README.md
```

---

# 📖 Documentation

상세 설계는 `docs`에서 관리합니다.

| Document | Description |
|---|---|
| [Requirements](docs/requirements.md) | 서비스 요구사항 |
| [Architecture](docs/architecture.md) | 시스템 및 CQRS 아키텍처 |
| [ERD](docs/erd.md) | 데이터 모델 |
| [API Specification](docs/api-spec.md) | REST API 설계 |
| [Security](docs/security.md) | 인증·인가 및 보안 설계 |

---

# 🧪 Testing Strategy

다음 계층을 테스트합니다.

```text
Domain Unit Test

Application Service Test

Controller Test

Repository Test

Security Test

Integration Test
```

주요 보안 테스트:

```text
비로그인 사용자
→ 보호 API
→ 401

USER
→ ADMIN API
→ 403

Student A
→ Student B Resource
→ 접근 거부

잘못된 Token
→ 401

중복 submissionId
→ QuestionAttempt 1건
```

---

# 🚀 Development Roadmap

```text
Phase 1
Project Setup

↓

Phase 2
Identity
LOCAL Signup / Login

↓

Phase 3
Spring Security + JWT

↓

Phase 4
Google / Kakao / Naver OAuth

↓

Phase 5
Curriculum
Subject / Unit / Passage / Question

↓

Phase 6
Learning
QuestionAttempt

↓

Phase 7
Diagnostic Test

↓

Phase 8
Analytics
Dashboard / Wrong Answer / Progress

↓

Phase 9
Recommendation

↓

Phase 10
React Frontend Integration

↓

Phase 11
Testing / Security Hardening

↓

Phase 12
Deployment / CI/CD
```

---

# 🔮 Future Expansion

서비스 성장 이후 다음 구조를 검토합니다.

```text
Domain Event

↓

Transactional Outbox

↓

Kafka

↓

Projection

↓

Redis / Read Database

↓

필요한 Domain의 독립 Service 분리
```

기술을 먼저 도입하기보다,
실제 트래픽과 요구사항이 발생했을 때 확장합니다.

---

# 📌 V1 Scope

V1에서 제외:

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

AI 문제 자동 생성

AI Tutor

영어 Listening
```

핵심 학습 흐름을 안정적으로 완성한 뒤 V2 이후 확장을 검토합니다.

---

## Repository

StudyBridge Labs

```text
studybridge-labs/edutech-platform
```
