# EduTech Platform - Security Design

## 1. 보안 목표

EduTech Platform은 실제 인터넷 배포를 전제로 한다.

서비스에서 다루는 주요 정보:

```text
사용자 계정

이메일

비밀번호

학생 학년

학습 기록

정답률

오답 기록

OAuth 계정

인증 Session
```

따라서 보안은 기능 개발 이후 추가하는 요소가 아니라
초기 설계 요구사항으로 취급한다.

핵심 목표:

```text
Authentication

Authorization

Data Protection

Input Validation

Secret Management

Secure Communication

Audit / Monitoring
```

---

# 2. 주요 위협 모델

V1에서는 최소한 다음 공격 및 실수를 고려한다.

```text
비밀번호 탈취

Brute Force Login

Credential Stuffing

JWT 탈취

Refresh Token 탈취

OAuth 계정 오연결

IDOR

권한 상승

CSRF

XSS

SQL Injection

과도한 API 요청

민감정보 로그 노출

Secret GitHub 노출

관리자 API 무단 접근

다른 학생의 학습 데이터 접근
```

---

# 3. Authentication Architecture

지원 인증 방식:

```text
LOCAL

GOOGLE

KAKAO

NAVER
```

전체 구조:

```text
                React
                  │
                  ▼
           Spring Security
                  │
        ┌─────────┴─────────┐
        │                   │
       LOCAL              OAuth2
        │            ┌──────┼──────┐
        │          Google  Kakao  Naver
        │                   │
        └─────────┬─────────┘
                  ▼
                 User
                  │
          ┌───────┴────────┐
          ▼                ▼
    SocialAccount      AuthSession
```

---

# 4. LOCAL Password Security

비밀번호는 절대로 평문으로 저장하지 않는다.

```text
Raw Password
      ↓
PasswordEncoder
      ↓
BCrypt Hash
      ↓
users.password_hash
```

Spring Security의:

```java
PasswordEncoder
```

를 사용한다.

V1 기본 구현:

```java
BCryptPasswordEncoder
```

---

# 5. Password 정책

비밀번호 정책은 너무 단순하지 않게 설정한다.

예:

```text
최소 길이

최대 길이

허용 문자

공백 정책
```

비밀번호 정책은 Backend에서 반드시 검증한다.

Frontend Validation은 사용자 경험을 위한 것이며,
보안 검증의 최종 책임은 Backend에 있다.

---

# 6. 비밀번호 로그 금지

다음 정보는 로그에 출력하지 않는다.

```text
Raw Password

Password Hash

Login Request 전체 객체
```

잘못된 예:

```java
log.info("login request = {}", request);
```

Login DTO의 `toString()` 등을 통해
비밀번호가 노출되지 않도록 주의한다.

---

# 7. Access Token

Access Token은 API 인증에 사용한다.

예:

```http
Authorization: Bearer {accessToken}
```

Access Token은 짧은 수명을 사용한다.

예:

```text
15분
```

정확한 만료 시간은 실제 운영 요구에 따라 조정한다.

Access Token Payload에는 최소한의 정보만 포함한다.

예:

```text
sub

role

sessionId

iat

exp
```

다음과 같은 민감한 개인정보는 넣지 않는다.

```text
Password

Refresh Token

학습 기록

전화번호

민감 개인정보
```

---

# 8. Access Token 저장

Browser에서 Access Token은 가능한 한:

```text
Memory
```

기반으로 관리한다.

예:

```text
React State

Authentication Context

필요 시 상태관리 Store
```

Access Token을 장기간 LocalStorage에 저장하는 방식을 기본 전략으로 사용하지 않는다.

이유:

```text
XSS 발생 시 Token 탈취 위험
```

---

# 9. Refresh Token

Refresh Token은 Access Token보다 긴 수명을 가진다.

Refresh Token은 Browser에서:

```text
HttpOnly Cookie
```

로 전달한다.

Cookie 권장 속성:

```text
HttpOnly

Secure

SameSite 정책

Path 제한 검토
```

Production에서는:

```text
Secure = true
```

를 사용한다.

---

# 10. Refresh Token DB 저장

Refresh Token 원문을 그대로 DB에 저장하지 않는다.

```text
Refresh Token
       ↓
Hash
       ↓
auth_sessions.refresh_token_hash
```

DB가 유출되어도 Refresh Token 원문이 바로 사용되지 않도록 한다.

---

# 11. Refresh Token Rotation

Token 재발급 시 Refresh Token Rotation 적용을 검토한다.

```text
Refresh Token A
       ↓
POST /auth/refresh
       ↓
A 검증
       ↓
A 폐기
       ↓
Refresh Token B 발급
       ↓
B Hash 저장
```

탈취된 이전 Refresh Token의 재사용 위험을 낮춘다.

---

# 12. AuthSession

로그인마다 Session을 생성한다.

```text
User
 │
 │ 1:N
 ▼
AuthSession
```

이를 통해:

```text
PC

Mobile

Tablet
```

등 여러 로그인 Session을 독립적으로 관리할 수 있다.

---

# 13. Logout

로그아웃 시:

```text
AuthSession 조회
        ↓
revoked_at 기록
        ↓
Refresh Cookie 삭제
```

처리한다.

Access Token은 짧은 만료 시간을 이용하여 자연스럽게 만료시키는 방식을 기본으로 한다.

즉시 Access Token 폐기가 반드시 필요해지는 요구가 생기면
별도의 Blacklist 또는 Session 검증 전략을 검토한다.

---

# 14. OAuth Provider

지원 Provider:

```text
Google

Kakao

Naver
```

OAuth Provider별 구현은 Infrastructure Layer에 둔다.

```text
identity
└── infrastructure
    └── oauth
        ├── GoogleOAuthClient
        ├── KakaoOAuthClient
        └── NaverOAuthClient
```

Application / Domain은 Provider별 세부 API 구조에 직접 의존하지 않는다.

---

# 15. OAuth 데이터 최소 수집

OAuth Provider에서 필요 이상의 사용자 정보를 요청하지 않는다.

가능한 최소 정보:

```text
Provider User ID

Email

필요한 경우 Profile Name
```

서비스 기능과 관계없는 정보는 요청하지 않는다.

예:

```text
전화번호

주소

성별

생년월일
```

등은 V1에서 필요하지 않다면 수집하지 않는다.

---

# 16. OAuth 계정 식별

Social Account는 이메일만으로 식별하지 않는다.

다음 조합을 사용한다.

```text
provider

+

providerUserId
```

DB:

```text
UNIQUE(provider, provider_user_id)
```

를 적용한다.

---

# 17. OAuth 자동 계정 병합 금지

다음 두 계정이 있다고 가정한다.

```text
LOCAL

student@gmail.com
```

```text
GOOGLE

student@gmail.com
```

이메일이 같다는 이유만으로 자동 연결하지 않는다.

```text
Email 동일
≠
계정 소유권 검증 완료
```

---

# 18. Social Account Linking

사용자가 Social Account를 기존 계정에 연결하려면:

```text
기존 계정 로그인

        ↓

Account Linking 요청

        ↓

OAuth Provider 인증

        ↓

본인 확인

        ↓

SocialAccount 생성
```

과정을 거친다.

---

# 19. OAuth Token 전달

OAuth 로그인 성공 후 Access Token이나 Refresh Token을:

```text
URL Query Parameter
```

로 직접 전달하지 않는다.

지양:

```text
https://frontend.com/oauth/callback?accessToken=...
```

이유:

```text
Browser History

Proxy Log

Access Log

Referer
```

등에 Token이 노출될 수 있다.

---

# 20. Authorization

서비스 Role:

```text
USER

ADMIN
```

Spring Security를 이용하여 권한을 검증한다.

예:

```java
@PreAuthorize("hasRole('ADMIN')")
```

하지만 Controller Annotation만으로 모든 Authorization을 끝내지 않는다.

---

# 21. Resource Ownership

이번 프로젝트에서 매우 중요한 보안 요구사항이다.

예를 들어:

```http
GET /api/v1/diagnostics/{sessionId}
```

를 호출할 때 해당 Session이 현재 로그인 사용자의 것인지 확인해야 한다.

잘못된 구조:

```text
sessionId가 존재함
        ↓
조회 허용
```

올바른 구조:

```text
현재 Authentication
       ↓
currentUserId
       ↓
DiagnosticSession 조회
       ↓
session.userId == currentUserId ?
       │
       ├── YES → 허용
       │
       └── NO → 접근 거부
```

---

# 22. IDOR 방어

다른 사용자의 Resource ID를 URL에 입력해 데이터를 조회하는 공격을 방지한다.

예:

```text
Student A의 Session

/session/AAA
```

Student B가:

```text
/session/AAA
```

를 요청하더라도 접근할 수 없어야 한다.

UUID를 사용하더라도 Ownership 검증은 반드시 수행한다.

```text
UUID
≠
Authorization
```

---

# 23. /me API

사용자 자신의 데이터는 가능한 한:

```http
/api/v1/me/**
```

형태로 제공한다.

예:

```http
GET /api/v1/me/dashboard

GET /api/v1/me/wrong-answers

GET /api/v1/me/progress
```

URL에서 사용자 ID를 받지 않고
Access Token의 Authentication에서 현재 사용자를 식별한다.

---

# 24. ADMIN API

관리자 기능은:

```text
/api/v1/admin/**
```

경로를 사용한다.

예:

```http
POST /api/v1/admin/questions

PATCH /api/v1/admin/questions/{id}

GET /api/v1/admin/users
```

USER가 접근할 경우:

```text
403 Forbidden
```

을 반환한다.

---

# 25. Authentication Error

인증되지 않은 사용자가 보호 API를 호출하면:

```text
401 Unauthorized
```

---

# 26. Authorization Error

인증은 되어 있지만 권한이 없다면:

```text
403 Forbidden
```

예:

```text
USER

→ ADMIN Question API

→ 403
```

401과 403을 구분한다.

---

# 27. CORS

개발 환경:

```text
Frontend
http://localhost:5173

Backend
http://localhost:8080
```

서로 Origin이 다르므로 CORS 설정이 필요하다.

허용:

```text
http://localhost:5173
```

---

# 28. Production CORS

운영에서는 실제 Frontend Domain만 허용한다.

예:

```text
https://app.studybridge.example
```

지양:

```java
allowedOrigins("*")
```

특히 Credential Cookie를 사용하는 환경에서는 Origin을 명확하게 제한한다.

---

# 29. CORS 환경변수

Frontend Origin을 코드에 고정하지 않는다.

예:

```text
CORS_ALLOWED_ORIGINS
```

환경변수를 사용한다.

Local:

```text
http://localhost:5173
```

Production:

```text
https://실제-frontend-domain
```

---

# 30. CSRF

Access Token을:

```http
Authorization Header
```

로 전달하는 일반 API는 Cookie 기반 Session Authentication보다 CSRF 위험이 낮다.

하지만 Refresh Token은 HttpOnly Cookie를 사용할 예정이므로:

```http
POST /api/v1/auth/refresh
```

같은 Cookie 기반 Endpoint는 CSRF 관점에서 별도로 보호해야 한다.

---

# 31. Refresh Endpoint CSRF 방어

다음 전략을 함께 검토한다.

```text
SameSite Cookie

Origin Header 검증

Referer 검증

필요 시 CSRF Token
```

배포 환경의 Frontend / Backend Domain 구조에 맞춰 최종 정책을 결정한다.

---

# 32. Cookie Domain 전략

가능하다면 운영 Domain을:

```text
app.example.com

api.example.com
```

처럼 같은 Site 아래 구성하는 방식을 고려한다.

Frontend와 Backend를 완전히 다른 Site에 배포할 경우
Cookie의 SameSite 정책과 CORS 설정을 더욱 주의해서 구성한다.

---

# 33. Input Validation

모든 외부 입력은 신뢰하지 않는다.

검증 대상:

```text
@RequestBody

@PathVariable

@RequestParam

OAuth Provider Response

Header

Cookie
```

Spring Validation을 사용한다.

예:

```java
@NotBlank

@Email

@Size

@Min

@Max
```

---

# 34. Frontend Validation

React에서도:

```text
React Hook Form

Zod
```

등을 통해 사용자 입력을 검증할 수 있다.

하지만:

```text
Frontend Validation
=
UX
```

```text
Backend Validation
=
Security / Data Integrity
```

Backend 검증은 반드시 존재해야 한다.

---

# 35. SQL Injection

Spring Data JPA의 Parameter Binding을 사용한다.

지양:

```java
"SELECT ... WHERE email = '" + email + "'"
```

Native Query를 사용할 경우에도 문자열 결합으로 Query를 만들지 않는다.

---

# 36. XSS

React는 기본적으로 문자열을 Escape하지만
이를 XSS 방어의 전부로 간주하지 않는다.

특히 향후:

```text
HTML 학습 콘텐츠

Markdown

Rich Text

관리자 문제 Editor
```

를 추가할 경우 Sanitization 정책이 필요하다.

React의:

```text
dangerouslySetInnerHTML
```

사용은 특별한 이유가 없다면 피한다.

EduTech Platform의 문제와 Passage는 Markdown + LaTeX 표현을 지원할 수 있으므로,
Markdown Renderer 및 수식 Renderer 사용 시 사용자 또는 관리자 입력 콘텐츠를 신뢰하지 않는다.

HTML 렌더링이 필요한 경우 허용 가능한 태그와 속성을 제한하고 Sanitization을 적용한다.

---

# 37. Content Security Policy

Production에서는 CSP 적용을 검토한다.

예:

```text
default-src

script-src

connect-src

img-src

frame-ancestors
```

Frontend에서 사용하는 외부 CDN이나 OAuth Provider를 고려하여 정책을 설계한다.

---

# 38. Security Headers

운영 환경에서 다음 Header를 검토한다.

```text
Strict-Transport-Security

X-Content-Type-Options

Content-Security-Policy

Referrer-Policy

Permissions-Policy
```

Clickjacking 방지를 위해:

```text
frame-ancestors
```

등을 사용한다.

---

# 39. HTTPS

Production에서는 HTTPS를 필수로 한다.

금지:

```text
http://production-api
```

권장:

```text
https://api...
```

로그인 정보와 Token이 평문 통신으로 전달되지 않도록 한다.

---

# 40. Rate Limiting

특히 다음 API는 Rate Limit 대상이다.

```text
회원가입

로그인

Token Refresh

OAuth 관련 API

비밀번호 관련 API

문제 제출

향후 AI API
```

---

# 41. Login Brute Force

반복 로그인 실패에 대한 보호를 구현한다.

예:

```text
짧은 시간 동안 반복 실패
        ↓
일시적인 추가 제한
```

계정을 장시간 완전히 잠가버리는 방식은
공격자가 다른 사용자의 계정을 고의로 잠그는 DoS로 이용할 수 있으므로 신중하게 설계한다.

---

# 42. Rate Limit Storage

V1에서는 단일 서버일 경우 간단한 방식으로 시작할 수 있다.

향후 Scale Out이 필요하면:

```text
Redis
```

기반 Distributed Rate Limit으로 확장한다.

---

# 43. Duplicate Submission Security

Question Attempt Request에는:

```text
submissionId
```

를 사용한다.

```text
Client UUID
        ↓
Backend
        ↓
UNIQUE
```

동일 submissionId가 다시 들어오면:

```text
새 Attempt 생성 X
```

기존 결과를 반환한다.

이를 통해:

```text
Double Click

Network Retry

Client Retry
```

등의 중복 저장을 방지한다.

## Diagnostic Question 중복 제출

`submissionId` 중복 방지와 별개로,
하나의 DiagnosticSession에서 동일한 Question은 한 번만 제출할 수 있다.

```text
DiagnosticSession
+
Question

→ DIAGNOSTIC Attempt 1개

---

# 44. 문제 정답 보호

학생용 문제 Query Response에는 다음 정보를 절대로 포함하지 않는다.

```text
isCorrect

correctAnswer

explanation
```

문제 제출 이후에만 필요한 정보를 반환한다.

---

# 45. 진단평가 정답 보호

진단평가 중에는 더욱 엄격하게 처리한다.

답안을 제출하더라도:

```text
정답 여부

정답

해설
```

을 즉시 알려주지 않는다.

진단평가 완료 이후 결과를 조회한다.

---

# 46. Problem Manipulation

Frontend에서 다음 데이터를 전달하더라도 신뢰하지 않는다.

```text
correct = true

score = 100

difficulty = EASY

userId = ...
```

정답 여부와 학습 결과는 Backend가 계산한다.

Frontend에서 전달되는 `solvingTimeMs` 역시 신뢰할 수 없는 Client 데이터로 취급한다.

예:

```text
solvingTimeMs = 1

---

# 47. Secret Management

다음 값은 Git Repository에 저장하지 않는다.

```text
DB_PASSWORD

JWT_SECRET

GOOGLE_CLIENT_SECRET

KAKAO_CLIENT_SECRET

NAVER_CLIENT_SECRET

향후 AI_API_KEY
```

---

# 48. Environment Variables

예:

```text
DB_URL

DB_USER

DB_PASSWORD

JWT_SECRET

ACCESS_TOKEN_EXPIRATION

REFRESH_TOKEN_EXPIRATION

GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET

KAKAO_CLIENT_ID
KAKAO_CLIENT_SECRET

NAVER_CLIENT_ID
NAVER_CLIENT_SECRET

CORS_ALLOWED_ORIGINS
```

---

# 49. Git Security

`.gitignore`에 최소한 다음을 포함한다.

```gitignore
.env
.env.*
!.env.example

.idea/
*.iml

build/
.gradle/

node_modules/
dist/
```

실제 Secret이 Git에 Commit되면
단순히 파일을 삭제하는 것으로 끝내지 않는다.

노출된 Credential을 즉시 폐기하고 새로 발급한다.

---

# 50. .env.example

Repository에는 실제 Secret이 없는 예제 파일만 제공한다.

예:

```text
DB_URL=
DB_USER=
DB_PASSWORD=

JWT_SECRET=

GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=

NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
```

---

# 51. Database Security

Production Database는 최소 권한 원칙을 적용한다.

Application DB User에게 필요한 권한만 제공한다.

가능하면 운영 DB를 인터넷 전체에 직접 노출하지 않는다.

---

# 52. Database Connection

Production에서는 DB Provider가 지원할 경우:

```text
TLS
```

연결을 사용한다.

Database Password는 Application Code에 직접 작성하지 않는다.

---

# 53. Database Migration Security

Production에서:

```text
ddl-auto=update
```

에 의존하지 않는다.

Flyway Migration을 사용한다.

Production Application은 Schema 변경 권한까지 반드시 가져야 하는지 검토한다.

운영 성숙도가 높아지면 Migration 권한과 Application Runtime 권한을 분리할 수 있다.

---

# 54. Sensitive Logging

다음 정보는 로그에 남기지 않는다.

```text
Password

Password Hash

Access Token

Refresh Token

Authorization Header

OAuth Access Token

OAuth Refresh Token

Cookie 전체 값

DB Password
```

---

# 55. Security Logging

반대로 다음 이벤트는 추적 가능하도록 한다.

```text
로그인 성공

로그인 실패

Refresh 실패

Logout

ADMIN 중요 작업

권한 없는 접근

계정 상태 변경
```

단 개인정보와 Token을 로그에 그대로 남기지 않는다.

---

# 56. Audit Log

관리자가 다음 작업을 수행할 경우
추후 Audit Log를 고려한다.

```text
문제 등록

문제 수정

문제 비활성화

사용자 상태 변경
```

예:

```text
actorUserId

action

targetType

targetId

timestamp
```

---

# 57. Error Response

Production Error Response에 내부 구현 정보를 노출하지 않는다.

금지:

```text
StackTrace

SQL Query

Database Table Name

JWT Secret

Internal Class Path
```

Response 예:

```json
{
  "status": 404,
  "code": "QUESTION_NOT_FOUND",
  "message": "문제를 찾을 수 없습니다.",
  "traceId": "abc123"
}
```

---

# 58. Trace ID

요청별:

```text
traceId
```

또는 Correlation ID를 사용할 수 있도록 구조를 고려한다.

사용자에게 오류 문의를 받을 경우:

```text
traceId
```

를 이용해 서버 로그와 연결할 수 있다.

---

# 59. ADMIN Security

ADMIN 계정은 일반 USER보다 높은 권한을 가진다.

관리자 API에서는:

```text
Authentication

Role Authorization

Input Validation

Audit
```

을 모두 적용한다.

---

# 60. Role 변경

일반 사용자가 Request Body를 조작해서:

```json
{
  "role": "ADMIN"
}
```

으로 자신의 Role을 변경할 수 없도록 한다.

Student Profile API에는 Role 변경 필드 자체를 제공하지 않는다.

---

# 61. Mass Assignment

Request DTO를 Entity에 그대로 Binding하지 않는다.

지양:

```text
Request
→ User Entity 전체 Binding
```

권장:

```text
SignupRequest
→ 필요한 필드만 수신
→ Application Service
→ User 생성
```

이를 통해 사용자가 의도하지 않은 필드를 조작하는 문제를 줄인다.

---

# 62. Entity 직접 Response 금지

JPA Entity를 API Response로 직접 반환하지 않는다.

이유:

```text
민감 필드 노출 위험

Lazy Loading

순환 참조

내부 Schema 노출

API와 Domain 결합
```

Response DTO를 별도로 사용한다.

---

# 63. 학생 개인정보

서비스 대상이 중학생이므로 개인정보 수집 범위를 최소화한다.

V1에서 반드시 필요하지 않은 정보는 수집하지 않는다.

예:

```text
주소

전화번호

학교명

주민등록번호
```

등은 V1 기본 요구사항에서 제외한다.

---

# 64. 미성년 사용자 운영

실제 미성년자를 대상으로 공개 운영하는 경우
개발 보안과 별개로 적용되는 개인정보·보호자 동의·교육서비스 관련 법적 요구사항을 별도로 검토해야 한다.

해당 정책은 서비스 공개 전에 최신 법령과 운영 요건을 기준으로 별도 검토한다.

---

# 65. Account Withdrawal

회원 탈퇴 시 단순히:

```text
status = WITHDRAWN
```

만 하면 되는지는 운영 정책과 개인정보 보존 요구사항에 따라 달라질 수 있다.

실제 서비스 전에는:

```text
삭제 대상

익명화 대상

법적으로 보존해야 하는 데이터

보존 기간
```

정책을 별도로 정의한다.

---

# 66. Backup

Production Database는 Backup 전략을 가져야 한다.

검토 대상:

```text
자동 Backup

Backup 주기

보존 기간

Restore Test
```

Backup이 존재하는 것만으로 충분하지 않고
실제로 복원 가능한지 검증해야 한다.

---

# 67. Dependency Security

Frontend와 Backend Dependency를 최신 상태로 관리한다.

검토:

```text
Dependabot

GitHub Security Alerts

npm audit

Gradle Dependency 취약점 검사
```

취약점이 발견됐다고 모든 버전을 무조건 최신으로 올리는 것이 아니라
영향 범위를 확인하고 안전하게 업데이트한다.

---

# 68. CI Security

GitHub Actions에서 Secret을 코드에 직접 작성하지 않는다.

사용:

```text
GitHub Actions Secrets
```

PR 로그에도 Secret이 출력되지 않도록 주의한다.

---

# 69. Branch Protection

팀 개발 시 `main` Branch 보호를 적용한다.

예:

```text
PR 필수

CI 통과 필수

직접 Push 제한

Review 요구
```

초기 1인 개발 단계에서는 부담이 크지 않은 수준부터 적용하고
팀 규모에 따라 강화한다.

---

# 70. Security Test

보안 기능은 반드시 자동 테스트한다.

---

## 비로그인 사용자

```text
GET /api/v1/me/dashboard

→ 401
```

---

## USER → ADMIN

```text
POST /api/v1/admin/questions

USER

→ 403
```

---

## 다른 학생 Resource 접근

```text
Student A

→ Student B DiagnosticSession 접근

→ 거부
```

---

## 잘못된 Access Token

```text
→ 401
```

---

## 만료 Access Token

```text
→ 401
```

---

## Invalid Refresh Token

```text
→ 401
```

---

## Revoked Session

```text
→ 401
```

---

## 중복 submissionId

```text
동일 submissionId 두 번 요청

→ QuestionAttempt 한 건만 생성
```

---

# 71. OAuth Security Test

최소 테스트:

```text
지원하지 않는 Provider

잘못된 OAuth State

이미 연결된 Social Account

다른 User에게 연결된 Social Account

신규 OAuth 사용자

기존 OAuth 사용자
```

---

# 72. Security Review Checklist

배포 전 확인:

```text
[ ] HTTPS 사용

[ ] Production CORS Origin 제한

[ ] Secret Repository 미포함

[ ] .env Git Ignore

[ ] Password BCrypt

[ ] JWT Secret 충분한 강도

[ ] Access Token 짧은 만료

[ ] Refresh Token HttpOnly Cookie

[ ] Refresh Session revoke

[ ] OAuth Token URL 노출 없음

[ ] USER / ADMIN Authorization

[ ] Resource Ownership 검증

[ ] 학생용 문제 Response 정답 미포함

[ ] Error StackTrace 미노출

[ ] Sensitive Log 없음

[ ] DB Production Credential 분리

[ ] Security Test 통과

[ ] Dependency 취약점 검토
```

---

# 73. V1 Security Architecture

최종 V1 인증 구조:

```text
                   React

                     │
             Access Token
          Authorization Header

                     │
                     ▼

              Spring Security
                     │
       ┌─────────────┴─────────────┐
       │                           │
     LOCAL                       OAuth2
                             ┌─────┼─────┐
                           Google Kakao Naver

                     │
                     ▼
                    User
                     │
                     ▼
                AuthSession
                     │
             Refresh Token Hash
                     │

        HttpOnly Secure Cookie
               Refresh Token
```

---

# 74. Security Evolution

V1:

```text
Spring Security

OAuth2

JWT

BCrypt

Authorization

Ownership

CORS

HTTPS

Validation

Security Test
```

서비스 성장 후:

```text
Redis Rate Limit

Advanced Audit Log

Security Monitoring

WAF

Centralized Secret Manager

Key Rotation

SIEM / Alerting
```

등을 검토한다.

---

# 75. 핵심 원칙

```text
1. Frontend를 신뢰하지 않는다.

2. 사용자 입력을 신뢰하지 않는다.

3. UUID를 Authorization으로 착각하지 않는다.

4. 이메일 동일성을 계정 소유권 증명으로 사용하지 않는다.

5. Password와 Token을 로그에 남기지 않는다.

6. Secret을 Git에 저장하지 않는다.

7. 학생 데이터는 현재 로그인 사용자의 소유권을 확인한다.

8. ADMIN 권한은 서버에서 검증한다.

9. 정답 판정은 Backend에서 수행한다.

10. Security 기능 역시 테스트 코드로 검증한다.
```