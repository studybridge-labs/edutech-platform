# EduTech Platform - Architecture

## 1. Architecture Goal

```text
Modular Monolith
+
Domain-Oriented Architecture
+
Logical CQRS
+
PostgreSQL
```

V1에서는 하나의 Spring Boot Application과 하나의 PostgreSQL을 사용한다.

```text
React
   ↓
Spring Boot
   ↓
PostgreSQL
```

MSA, Kafka, Redis, Read DB / Write DB 분리는 초기부터 적용하지 않는다.

---

# 2. Backend Domain

```text
global

identity
student
curriculum
learning
analytics
```

## Identity

```text
User
SocialAccount
AuthSession
LOCAL Login
OAuth
JWT
```

## Student

```text
StudentProfile
Grade
```

## Curriculum

```text
Subject
Unit
Passage
Question
QuestionOption
```

## Learning

```text
DiagnosticSession
DiagnosticSessionQuestion
QuestionAttempt
```

## Analytics

```text
Dashboard
Wrong Answer
Progress
Weak Unit
Recommendation
```

Analytics는 원본 학습 데이터를 소유하지 않는다.

```text
Learning
→ QuestionAttempt

Analytics
→ QuestionAttempt Query
```

---

# 3. CQRS

## Command

상태를 변경한다.

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

상태를 변경하지 않는다.

```text
Query Controller
       ↓
Query Service
       ↓
Query Repository
       ↓
Projection
       ↓
PostgreSQL
```

---

# 4. Logical CQRS

V1:

```text
                PostgreSQL

Command ───────────►

Query   ◄───────────
```

DB를 분리하지 않고 코드의 책임을 분리한다.

---

# 5. Package Structure

```text
com.studybridge.edutech

├── global
│   ├── config
│   ├── security
│   ├── exception
│   └── common
│
├── identity
│   ├── api
│   │   ├── command
│   │   └── query
│   ├── application
│   │   ├── command
│   │   └── query
│   ├── domain
│   └── infrastructure
│
├── student
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
│
├── curriculum
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
│
├── learning
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
│
└── analytics
    ├── api
    ├── application
    └── infrastructure
```

---

# 6. Layer

```text
API
 ↓
Application
 ↓
Domain

Infrastructure
 ↓
Domain Interface
```

API:

```text
HTTP
Validation
Request / Response
```

Application:

```text
Use Case
Transaction
```

Domain:

```text
Entity
Business Rule
Domain Interface
```

Infrastructure:

```text
JPA
PostgreSQL
JWT
Google
Kakao
Naver
```

---

# 7. Transaction

Command:

```java
@Transactional
```

Query:

```java
@Transactional(readOnly = true)
```

를 기본으로 한다.

---

# 8. Learning Write Flow

```text
React
 ↓
POST /api/v1/questions/{questionId}/attempts
 ↓
Command Controller
 ↓
SubmitAnswerCommand
 ↓
Application Service
 ↓
Question
 ↓
정답 판정
 ↓
QuestionAttempt
 ↓
PostgreSQL
```

---

# 9. Analytics Query Flow

```text
GET /api/v1/me/dashboard
 ↓
Query Controller
 ↓
Query Service
 ↓
DashboardQueryRepository
 ↓
DB Aggregate / Projection
 ↓
DashboardResponse
```

QuestionAttempt 전체를 Java Memory에 올려서 계산하는 방식은 지양한다.

---

# 10. Frontend

```text
React
TypeScript
Vite
React Router
TanStack Query
React Hook Form
Zod
```

Query:

```text
GET
→ useQuery
```

Command:

```text
POST / PATCH / DELETE
→ useMutation
```

---

# 11. Future Architecture

```text
Phase 1
Modular Monolith + Logical CQRS

↓

Domain Event

↓

Transactional Outbox

↓

Kafka

↓

Read Projection

↓

Redis / Read DB

↓

필요한 Domain만 Service 분리
```

기술은 실제 요구가 생겼을 때 추가한다.