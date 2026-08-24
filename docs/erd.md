# EduTech Platform - ERD

## 1. V1 Tables

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

---

# 2. users

```text
id UUID PK

email VARCHAR(255)
UNIQUE NOT NULL

password_hash VARCHAR(255)
NULL 가능

nickname VARCHAR(50)
NOT NULL

role VARCHAR(20)
NOT NULL

status VARCHAR(20)
NOT NULL

created_at
updated_at
```

Role:

```text
USER
ADMIN
```

Status:

```text
ACTIVE
SUSPENDED
WITHDRAWN
```

---

# 3. student_profiles

```text
id UUID PK

user_id UUID
FK users.id
UNIQUE

grade VARCHAR(20)

created_at
updated_at
```

Grade:

```text
MIDDLE_1
MIDDLE_2
MIDDLE_3
```

---

# 4. social_accounts

```text
id UUID PK

user_id UUID FK

provider VARCHAR(20)

provider_user_id VARCHAR(255)

created_at
```

Constraints:

```text
UNIQUE(provider, provider_user_id)

UNIQUE(user_id, provider)
```

---

# 5. auth_sessions

```text
id UUID PK

user_id UUID FK

refresh_token_hash VARCHAR(255)

expires_at

revoked_at NULL

created_at
```

---

# 6. subjects

```text
id UUID PK

code VARCHAR(30)
UNIQUE

name VARCHAR(50)

active BOOLEAN

created_at
updated_at
```

Data:

```text
MATH
ENGLISH
```

---

# 7. units

```text
id UUID PK

subject_id UUID FK

parent_unit_id UUID FK NULL

grade VARCHAR(20)

name VARCHAR(100)

order_no INTEGER

active BOOLEAN

created_at
updated_at
```

Self Reference:

```text
Unit
└── child Unit
```

---

# 8. passages

```text
id UUID PK

unit_id UUID FK

title VARCHAR(200) NULL

content TEXT NOT NULL

active BOOLEAN NOT NULL

created_at
updated_at
```

관계:

```text
Unit 1:N Passage

Passage 1:N Question
```

---

# 9. questions

```text
id UUID PK

unit_id UUID FK

passage_id UUID FK NULL

question_type VARCHAR(30)

difficulty VARCHAR(20)

content TEXT

correct_answer_text TEXT NULL

explanation TEXT

active BOOLEAN

created_at
updated_at
```

QuestionType:

```text
MULTIPLE_CHOICE
SHORT_ANSWER
```

Difficulty:

```text
EASY
MEDIUM
HARD
```

---

# 10. question_options

```text
id UUID PK

question_id UUID FK

option_no INTEGER

content TEXT

is_correct BOOLEAN
```

Constraint:

```text
UNIQUE(question_id, option_no)
```

학생 Query Response에서는 `is_correct`를 반환하지 않는다.

---

# 11. diagnostic_sessions

```text
id UUID PK

user_id UUID FK

subject_id UUID FK

grade VARCHAR(20)

status VARCHAR(20)

started_at

completed_at NULL
```

Status:

```text
IN_PROGRESS
COMPLETED
ABANDONED
```

grade는 진단평가 시작 당시 학년 Snapshot이다.

---

# 12. diagnostic_session_questions

```text
id UUID PK

diagnostic_session_id UUID FK

question_id UUID FK

order_no INTEGER
```

Constraints:

```text
UNIQUE(diagnostic_session_id, question_id)

UNIQUE(diagnostic_session_id, order_no)
```

---

# 13. question_attempts

```text
id UUID PK

submission_id UUID
UNIQUE NOT NULL

user_id UUID FK

question_id UUID FK

diagnostic_session_id UUID FK NULL

selected_option_id UUID FK NULL

answer_text TEXT NULL

correct BOOLEAN NOT NULL

solving_time_ms BIGINT NULL

attempt_type VARCHAR(20)

attempted_at TIMESTAMP
```

AttemptType:

```text
NORMAL
DIAGNOSTIC
REVIEW
```

객관식:

```text
selected_option_id != NULL

answer_text = NULL
```

단답형:

```text
selected_option_id = NULL

answer_text != NULL
```

---

# 14. Diagnostic 중복 제출

한 진단 Session에서 같은 문제는 한 번만 제출할 수 있다.

PostgreSQL Partial Unique Index:

```sql
CREATE UNIQUE INDEX uk_diagnostic_attempt
ON question_attempts (
    diagnostic_session_id,
    question_id
)
WHERE attempt_type = 'DIAGNOSTIC';
```

---

# 15. QuestionAttempt History

기존 Attempt는 수정하지 않는다.

```text
Attempt 1
correct = false

Attempt 2
correct = false

Attempt 3
correct = true
```

과거 기록을 유지한다.

---

# 16. Question 변경 정책

QuestionAttempt가 존재하지 않는 Question:

```text
수정 가능
```

QuestionAttempt가 존재하는 Question:

```text
문제 내용
정답
선택지

변경 제한
```

오류 문제:

```text
active = false
+
새 Question 생성
```

---

# 17. V1에서 만들지 않는 Table

```text
wrong_answers
dashboard
recommendations
progress
```

QuestionAttempt 기반 Query로 계산한다.

---

# 18. 주요 Index

```text
users
UNIQUE(email)

social_accounts
UNIQUE(provider, provider_user_id)
UNIQUE(user_id, provider)

units
INDEX(subject_id, grade)
INDEX(parent_unit_id)

passages
INDEX(unit_id)

questions
INDEX(unit_id)
INDEX(passage_id)
INDEX(unit_id, difficulty)
INDEX(unit_id, question_type)

diagnostic_sessions
INDEX(user_id, started_at)
INDEX(user_id, status)

diagnostic_session_questions
INDEX(diagnostic_session_id)

question_attempts
UNIQUE(submission_id)
INDEX(user_id, attempted_at)
INDEX(user_id, correct)
INDEX(user_id, question_id)
INDEX(question_id)
INDEX(diagnostic_session_id)
```

---

# 19. Migration

```text
Flyway
```

Production:

```text
ddl-auto=validate
```

`ddl-auto=update`에 의존하지 않는다.