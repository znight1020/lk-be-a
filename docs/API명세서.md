# API 명세서
샘플 데이터: [init_data.sql](/src/main/resources/database/init_data.sql)
- `init_data.sql`은 `CURRENT_DATE`, `CURRENT_TIMESTAMP`를 사용하므로 날짜/시간 필드는 실행 시점에 따라 달라질 수 있습니다. 아래 예시는 2026-04-29 기준 예시입니다.

- User
    - `user 1`: 강사
    - `user 101`: 학생
    - `user 102`: 학생
    - `user 103`: 학생
- Course
    - `course 1`: `OPEN` 상태, 정원은 2명이며 현재 신청이 가득 찬 강의
    - `course 2`: `OPEN` 상태, 정원은 5명이며 아직 수강 신청이 없는 빈 강의
    - `course 3`: `DRAFT` 상태, 아직 모집 전인 강의
    - `course 4`: `CLOSED` 상태, 모집이 마감된 강의
- Enrollment
    - `enrollment 1`: course id 1에 대한 user id 102의 `PENDING` 신청
    - `enrollment 2`: course id 1에 대한 user id 103의 `CONFIRMED` 신청
    - `enrollment 3`: course id 4에 대한 user id 102의 `CANCELLED` 신청

## 공통

- Base URL 예시: `http://localhost:8080`
- 인증/인가가 필요한 API는 요청 헤더 `X-User-Id`를 사용합니다.
- 날짜는 `yyyy-MM-dd`, 일시는 `yyyy-MM-ddTHH:mm:ss` 형식 예시로 표기했습니다.

## 공통 에러 응답 형식

예외 응답은 RFC 7807 기반 `ProblemDetail` 형식으로 응답합니다.

```json
{
  "type": "about:blank",
  "title": "ENROLLMENT_NOT_FOUND",
  "status": 404,
  "detail": "수강 신청을 찾을 수 없습니다",
  "timestamp": "2026-04-29T04:10:00"
}
```

## API 목록 요약

| Domain     | Method  | Path                                  | Header      | Require Role |
|------------|---------|---------------------------------------|-------------|--------------|
| User       | `POST`  | `/users`                              | -           | -            |
| Course     | `POST`  | `/courses`                            | `X-User-Id` | `CREATOR`    |
| Course     | `GET`   | `/courses`                            | -           | -            |
| Course     | `GET`   | `/courses/{courseId}`                 | -           | -            |
| Course     | `GET`   | `/courses/{courseId}/enrollments`     | `X-User-Id` | `CREATOR`    |
| Course     | `PATCH` | `/courses/{courseId}/open`            | `X-User-Id` | `CREATOR`    |
| Course     | `PATCH` | `/courses/{courseId}/close`           | `X-User-Id` | `CREATOR`    |
| Enrollment | `POST`  | `/enrollments`                        | `X-User-Id` | `STUDENT`    |
| Enrollment | `POST`  | `/enrollments/waitlist`               | `X-User-Id` | `STUDENT`    |
| Enrollment | `GET`   | `/enrollments/me`                     | `X-User-Id` | `STUDENT`    |
| Enrollment | `PATCH` | `/enrollments/{enrollmentId}/confirm` | `X-User-Id` | `STUDENT`    |
| Enrollment | `PATCH` | `/enrollments/{enrollmentId}/cancel`  | `X-User-Id` | `STUDENT`    |

## 1. 사용자(User)

### 1.1 회원 등록

- Method: `POST`
- Path: `/users`
- Header: 없음
- Role: 없음

Request

```json
{
  "email": "creator1@example.com",
  "name": "김강사",
  "role": "CREATOR"
}
```

Response `201 Created`

```json
{
  "userId": 1,
  "email": "creator1@example.com"
}
```

## 2. 강의(Course)

### 2.1 강의 등록

- Method: `POST`
- Path: `/courses`
- Header: `X-User-Id`
- Role: `CREATOR`

Request

```json
{
  "title": "Kafka 입문",
  "description": "Kafka 기초 강의",
  "price": 70000,
  "capacity": 10,
  "startDate": "2026-05-20",
  "endDate": "2026-06-20"
}
```

Response `201 Created`

```json
{
  "courseId": 500
}
```

### 2.2 강의 목록 조회

- Method: `GET`
- Path: `/courses`
- Query: `status` (optional, `DRAFT | OPEN | CLOSED`)
- Header: 없음
- Role: 없음

Response `200 OK`

```json
[
  {
    "courseId": 4,
    "creatorId": 1,
    "creatorName": "김강사",
    "title": "데이터베이스 인덱스",
    "price": 60000,
    "capacity": 2,
    "startDate": "2026-05-10",
    "endDate": "2026-06-09",
    "status": "CLOSED"
  },
  {
    "courseId": 3,
    "creatorId": 1,
    "creatorName": "김강사",
    "title": "Redis 사용",
    "price": 50000,
    "capacity": 3,
    "startDate": "2026-05-06",
    "endDate": "2026-06-05",
    "status": "DRAFT"
  },
  {
    "courseId": 2,
    "creatorId": 1,
    "creatorName": "김강사",
    "title": "MSA 설계",
    "price": 55000,
    "capacity": 5,
    "startDate": "2026-05-08",
    "endDate": "2026-06-07",
    "status": "OPEN"
  },
  {
    "courseId": 1,
    "creatorId": 1,
    "creatorName": "김강사",
    "title": "Spring Boot 입문",
    "price": 120000,
    "capacity": 2,
    "startDate": "2026-05-09",
    "endDate": "2026-06-08",
    "status": "OPEN"
  }
]
```

### 2.3 강의 상세 조회

- Method: `GET`
- Path: `/courses/{courseId}`
- Header: 없음
- Role: 없음

Response `200 OK`

```json
{
  "courseId": 1,
  "creatorId": 1,
  "creatorName": "김강사",
  "title": "Spring Boot 입문",
  "description": "스프링부트 기초 강의",
  "price": 120000,
  "capacity": 2,
  "startDate": "2026-05-09",
  "endDate": "2026-06-08",
  "status": "OPEN",
  "currentEnrollmentCount": 2
}
```

### 2.4 강의별 수강생 목록 조회

- Method: `GET`
- Path: `/courses/{courseId}/enrollments`
- Header: `X-User-Id`
- Role: `CREATOR`
- 설명: 해당 강의의 `CONFIRMED` 상태 수강생만 최신순으로 조회합니다.

Response `200 OK`

```json
[
  {
    "enrollmentId": 2,
    "studentId": 103,
    "studentName": "학생103",
    "studentEmail": "student103@example.com",
    "status": "CONFIRMED",
    "createdAt": "2026-04-26T10:00:00",
    "confirmedAt": "2026-04-27T10:00:00",
    "cancelledAt": null
  }
]
```

### 2.5 강의 모집 시작

- Method: `PATCH`
- Path: `/courses/{courseId}/open`
- Header: `X-User-Id`
- Role: `CREATOR`
- Body: 없음

Response `200 OK`

```json
{
  "courseId": 3,
  "status": "OPEN"
}
```

### 2.6 강의 모집 마감

- Method: `PATCH`
- Path: `/courses/{courseId}/close`
- Header: `X-User-Id`
- Role: `CREATOR`
- Body: 없음

Response `200 OK`

```json
{
  "courseId": 1,
  "status": "CLOSED"
}
```

## 3. 수강 신청(Enrollment)

### 3.1 수강 신청

- Method: `POST`
- Path: `/enrollments`
- Header: `X-User-Id`
- Role: `STUDENT`

Request

```json
{
  "courseId": 2
}
```

Response `201 Created`

```json
{
  "enrollmentId": 500,
  "courseId": 2,
  "status": "PENDING"
}
```

정원이 가득 찬 경우에는 `409 CONFLICT`와 `ENROLLMENT_CAPACITY_EXCEEDED`를 응답합니다.

### 3.2 대기열 등록

- Method: `POST`
- Path: `/enrollments/waitlist`
- Header: `X-User-Id`
- Role: `STUDENT`
- 설명: 수강 신청이 정원 초과로 거부된 뒤, 사용자가 별도로 대기열 등록을 요청할 때 사용합니다.
- 제약: 정원이 실제로 가득 찬 경우에만 등록 가능하며, 자리가 남아 있으면 `ENROLLMENT_WAITLIST_NOT_AVAILABLE`를 응답합니다.

Request

```json
{
  "courseId": 1
}
```

Response `201 Created`

```json
{
  "enrollmentId": 500,
  "courseId": 1,
  "status": "WAITING"
}
```

### 3.3 내 수강 신청 목록 조회

- Method: `GET`
- Path: `/enrollments/me`
- Header: `X-User-Id`
- Role: `STUDENT`
- Query: `page` (optional, default `0`), `size` (optional, default `4`)
- 예시: `X-User-Id: 102`

Response `200 OK`

```json
{
  "content": [
    {
      "enrollmentId": 3,
      "courseId": 4,
      "courseTitle": "데이터베이스 인덱스",
      "coursePrice": 60000,
      "status": "CANCELLED",
      "createdAt": "2026-04-23T10:00:00",
      "confirmedAt": null,
      "cancelledAt": "2026-04-24T10:00:00"
    },
    {
      "enrollmentId": 1,
      "courseId": 1,
      "courseTitle": "Spring Boot 입문",
      "coursePrice": 120000,
      "status": "PENDING",
      "createdAt": "2026-04-28T10:00:00",
      "confirmedAt": null,
      "cancelledAt": null
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

### 3.4 결제 확정 처리

- Method: `PATCH`
- Path: `/enrollments/{enrollmentId}/confirm`
- Header: `X-User-Id`
- Role: `STUDENT`
- Body: 없음

Response `200 OK`

```json
{
  "enrollmentId": 1,
  "courseId": 1,
  "status": "CONFIRMED"
}
```

### 3.5 수강 취소

- Method: `PATCH`
- Path: `/enrollments/{enrollmentId}/cancel`
- Header: `X-User-Id`
- Role: `STUDENT`
- Body: 없음

취소 규칙:

- `PENDING` 상태는 즉시 취소 가능
- `WAITING` 상태는 즉시 취소 가능
- `CONFIRMED` 상태는 결제 확정일 기준 7일 이내
- `CONFIRMED` 상태는 강의 시작일 전까지만 취소 가능
- `PENDING`, `CONFIRMED` 상태가 취소되면 가장 오래된 `WAITING` 등록 1건이 `PENDING`으로 승급

Response `200 OK`

```json
{
  "enrollmentId": 1,
  "courseId": 1,
  "status": "CANCELLED"
}
```
