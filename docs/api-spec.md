# EduTech Platform - API Specification

## 1. Base URL

```text
/api/v1
```

권한:

```text
PUBLIC
USER
ADMIN
```

CQRS:

```text
COMMAND
QUERY
```

---

# 2. 공통 Error Response

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "입력값을 확인해주세요.",
  "traceId": "abc123"
}
```

---

# 3. 회원가입

```text
COMMAND
PUBLIC
```

```http
POST /api/v1/auth/signup
```

Request:

```json
{
  "email": "student@example.com",
  "password": "Password123!",
  "nickname": "학생A",
  "grade": "MIDDLE_2"
}
```

Success:

```http
201 Created
```

Failure:

```text
400 VALIDATION_ERROR
409 EMAIL_ALREADY_EXISTS
```

---

# 4. 로그인

```text
COMMAND
PUBLIC
```

```http
POST /api/v1/auth/login
```

Request:

```json
{
  "email": "student@example.com",
  "password": "Password123!"
}
```

Response:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Refresh Token:

```text
HttpOnly Cookie
```

---

# 5. Token Refresh

```text
COMMAND
PUBLIC + Refresh Cookie
```

```http
POST /api/v1/auth/refresh
```

Response:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

---

# 6. Logout

```text
COMMAND
USER / ADMIN
```

```http
POST /api/v1/auth/logout
```

Success:

```text
204 No Content
```

---

# 7. Social Login

```http
GET /oauth2/authorization/google

GET /oauth2/authorization/kakao

GET /oauth2/authorization/naver
```

OAuth 인증 성공 후 신규 사용자는 Onboarding으로 이동한다.

---

# 8. Social Onboarding

```text
COMMAND
OAuth Temporary Authentication
```

```http
POST /api/v1/auth/social/onboarding
```

Request:

```json
{
  "nickname": "학생A",
  "grade": "MIDDLE_2"
}
```

---

# 9. 내 정보

```text
QUERY
USER
```

```http
GET /api/v1/me
```

Response:

```json
{
  "id": "uuid",
  "email": "student@example.com",
  "nickname": "학생A",
  "grade": "MIDDLE_2",
  "role": "USER",
  "linkedAccounts": [
    "GOOGLE"
  ]
}
```

---

# 10. 프로필 변경

```text
COMMAND
USER
```

```http
PATCH /api/v1/me/profile
```

Request:

```json
{
  "nickname": "새닉네임",
  "grade": "MIDDLE_3"
}
```

---

# 11. 과목 조회

```text
QUERY
USER
```

```http
GET /api/v1/subjects
```

Response:

```json
[
  {
    "id": "uuid",
    "code": "MATH",
    "name": "수학"
  }
]
```

---

# 12. 단원 조회

```text
QUERY
USER
```

```http
GET /api/v1/subjects/{subjectId}/units?grade=MIDDLE_2
```

---

# 13. 문제 목록

```text
QUERY
USER
```

```http
GET /api/v1/units/{unitId}/questions
```

Query:

```text
difficulty
type
page
size
```

---

# 14. 문제 상세

```text
QUERY
USER
```

```http
GET /api/v1/questions/{questionId}
```

객관식 Response:

```json
{
  "id": "question-uuid",
  "type": "MULTIPLE_CHOICE",
  "difficulty": "MEDIUM",
  "passage": {
    "id": "passage-uuid",
    "title": "Reading",
    "content": "..."
  },
  "content": "...",
  "options": [
    {
      "id": "option-uuid",
      "optionNo": 1,
      "content": "..."
    }
  ]
}
```

Response에 포함 금지:

```text
isCorrect
correctAnswer
explanation
```

---

# 15. 일반 문제 제출

```text
COMMAND
USER
```

```http
POST /api/v1/questions/{questionId}/attempts
```

객관식:

```json
{
  "submissionId": "uuid",
  "selectedOptionId": "uuid",
  "answerText": null,
  "solvingTimeMs": 42000
}
```

단답형:

```json
{
  "submissionId": "uuid",
  "selectedOptionId": null,
  "answerText": "3",
  "solvingTimeMs": 42000
}
```

Response:

```http
201 Created
```

```json
{
  "attemptId": "uuid",
  "correct": true,
  "correctAnswer": "3",
  "explanation": "..."
}
```

동일 submissionId 재요청:

```text
새 QuestionAttempt 생성 X
기존 결과 반환
```

---

# 16. 진단평가 시작

```text
COMMAND
USER
```

```http
POST /api/v1/diagnostics
```

Request:

```json
{
  "subjectId": "uuid"
}
```

학년은 로그인 사용자의 StudentProfile에서 가져온다.

Response:

```json
{
  "sessionId": "uuid",
  "subject": "MATH",
  "grade": "MIDDLE_2",
  "status": "IN_PROGRESS",
  "questionCount": 15
}
```

---

# 17. 진단평가 조회

```text
QUERY
USER
```

```http
GET /api/v1/diagnostics/{sessionId}
```

반드시 현재 사용자 소유 Session인지 검증한다.

---

# 18. 진단 문제 제출

```text
COMMAND
USER
```

```http
POST /api/v1/diagnostics/{sessionId}/questions/{questionId}/attempts
```

Request:

```json
{
  "submissionId": "uuid",
  "selectedOptionId": "uuid",
  "answerText": null,
  "solvingTimeMs": 30000
}
```

Response:

```json
{
  "attemptId": "uuid",
  "accepted": true
}
```

진단 진행 중에는 다음 정보를 반환하지 않는다.

```text
correct
correctAnswer
explanation
```

---

# 19. 진단평가 완료

```text
COMMAND
USER
```

```http
POST /api/v1/diagnostics/{sessionId}/complete
```

Response:

```json
{
  "sessionId": "uuid",
  "status": "COMPLETED",
  "completedAt": "..."
}
```

---

# 20. 진단평가 결과

```text
QUERY
USER
```

```http
GET /api/v1/diagnostics/{sessionId}/result
```

Response:

```json
{
  "sessionId": "uuid",
  "totalQuestions": 15,
  "correctCount": 9,
  "accuracy": 60.0,
  "units": [
    {
      "unitId": "uuid",
      "unitName": "연립방정식",
      "attemptCount": 5,
      "correctCount": 2,
      "accuracy": 40.0
    }
  ]
}
```

---

# 21. Dashboard

```text
QUERY
USER
```

```http
GET /api/v1/me/dashboard
```

---

# 22. 오답노트

```text
QUERY
USER
```

```http
GET /api/v1/me/wrong-answers
```

Query:

```text
subjectId
unitId
page
size
```

---

# 23. Progress

```text
QUERY
USER
```

```http
GET /api/v1/me/progress?subjectId={subjectId}
```

---

# 24. 취약 단원

```text
QUERY
USER
```

```http
GET /api/v1/me/weak-units?subjectId={subjectId}
```

---

# 25. 추천 문제

```text
QUERY
USER
```

```http
GET /api/v1/me/recommendations
```

Query:

```text
subjectId
limit
```

---

# 26. ADMIN - Passage 등록

```text
COMMAND
ADMIN
```

```http
POST /api/v1/admin/passages
```

---

# 27. ADMIN - 문제 등록

```text
COMMAND
ADMIN
```

```http
POST /api/v1/admin/questions
```

객관식:

```json
{
  "unitId": "uuid",
  "passageId": null,
  "questionType": "MULTIPLE_CHOICE",
  "difficulty": "MEDIUM",
  "content": "...",
  "explanation": "...",
  "options": [
    {
      "optionNo": 1,
      "content": "...",
      "correct": false
    },
    {
      "optionNo": 2,
      "content": "...",
      "correct": true
    }
  ]
}
```

---

# 28. ADMIN - 문제 수정

```text
COMMAND
ADMIN
```

```http
PATCH /api/v1/admin/questions/{questionId}
```

QuestionAttempt가 존재하면
정답·선택지·문제 내용 변경을 제한한다.

Conflict:

```text
409 QUESTION_ALREADY_ATTEMPTED
```

---

# 29. ADMIN - 문제 비활성화

```text
COMMAND
ADMIN
```

```http
PATCH /api/v1/admin/questions/{questionId}/status
```

Request:

```json
{
  "active": false
}
```

---

# 30. ADMIN - 단원 등록

```http
POST /api/v1/admin/units
```

Permission:

```text
ADMIN
```

---

# 31. ADMIN - 사용자 조회

```text
QUERY
ADMIN
```

```http
GET /api/v1/admin/users
```

---

# 32. 주요 Error Code

```text
VALIDATION_ERROR

EMAIL_ALREADY_EXISTS

INVALID_CREDENTIALS

USER_NOT_FOUND
USER_SUSPENDED
USER_WITHDRAWN

ACCESS_TOKEN_INVALID
ACCESS_TOKEN_EXPIRED

REFRESH_TOKEN_INVALID
REFRESH_TOKEN_EXPIRED
AUTH_SESSION_REVOKED

FORBIDDEN

SUBJECT_NOT_FOUND
UNIT_NOT_FOUND
PASSAGE_NOT_FOUND
QUESTION_NOT_FOUND
QUESTION_INACTIVE

INVALID_QUESTION_ANSWER

DIAGNOSTIC_NOT_FOUND
DIAGNOSTIC_ALREADY_COMPLETED
DIAGNOSTIC_INVALID_STATE
DIAGNOSTIC_QUESTION_NOT_ASSIGNED
DIAGNOSTIC_QUESTION_ALREADY_ANSWERED

QUESTION_ALREADY_ATTEMPTED

DUPLICATE_SUBMISSION
```

---

# 33. CQRS Mapping

## COMMAND

```text
POST  /auth/signup
POST  /auth/login
POST  /auth/refresh
POST  /auth/logout

POST  /auth/social/onboarding

PATCH /me/profile

POST  /questions/{id}/attempts

POST  /diagnostics
POST  /diagnostics/{id}/questions/{questionId}/attempts
POST  /diagnostics/{id}/complete

POST  /admin/passages
POST  /admin/questions
PATCH /admin/questions/{id}
PATCH /admin/questions/{id}/status

POST  /admin/units
```

## QUERY

```text
GET /me

GET /subjects
GET /subjects/{id}/units

GET /units/{id}/questions
GET /questions/{id}

GET /diagnostics/{id}
GET /diagnostics/{id}/result

GET /me/dashboard
GET /me/wrong-answers
GET /me/progress
GET /me/weak-units
GET /me/recommendations

GET /admin/users
```