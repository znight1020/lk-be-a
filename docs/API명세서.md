# API 명세서

샘플 데이터 : [init_data.sql](/src/main/resources/database/init_data.sql)

- User
    - `user id 1` : 강사
    - `user id 101` : 수강생1
    - `user id 102` : 수강생2
    - `user id 103` : 수강생3
- Course
    - `course id 1`: OPEN 상태, 정원은 2명이며 현재 신청이 가득 찬 강의 
    - `course id 2`: OPEN 상태, 정원은 5명이며 아직 수강 신청이 없는 빈 강의 
    - `course id 3`: DRAFT 상태, 아직 모집 전인 강의
    - `course id 4`: CLOSED 상태, 모집이 마감된 강의
- Enrollment
    - `enrollment id 1`: course id 1에 대한 user id 2의 수강 신청, PENDING 상태
    - `enrollment id 2`: course id 1에 대한 user id 3의 수강 신청, CONFIRMED 상태

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

주요 에러 코드 예시:

- `VALIDATION_ERROR`
- `REQUEST_USER_ID_HEADER_MISSING`
- `REQUEST_USER_ID_HEADER_INVALID`
- `COURSE_MANAGEMENT_FORBIDDEN`
- `COURSE_NOT_OWNER`
- `COURSE_NOT_FOUND`
- `COURSE_STATUS_NOT_DRAFT`
- `COURSE_STATUS_NOT_OPEN`
- `ENROLLMENT_FORBIDDEN`
- `ENROLLMENT_NOT_OWNER`
- `ENROLLMENT_NOT_FOUND`
- `ENROLLMENT_ALREADY_EXISTS`
- `ENROLLMENT_CAPACITY_EXCEEDED`
- `ENROLLMENT_STATUS_NOT_PENDING`
- `ENROLLMENT_STATUS_ALREADY_CANCELLED`
- `ENROLLMENT_CANCELLATION_PERIOD_EXPIRED`

## API 목록 요약

| Domain     | Method  | Path                                  | Header      | Require Role |
|------------|---------|---------------------------------------|-------------|--------------|
| User       | `POST`  | `/users`                              | -           | -            |
| Course     | `POST`  | `/courses`                            | `X-User-Id` | `CREATOR`    |
| Course     | `GET`   | `/courses`                            | -           | -            |
| Course     | `GET`   | `/courses/{courseId}`                 | -           | -            |
| Course     | `PATCH` | `/courses/{courseId}/open`            | `X-User-Id` | `CREATOR`    |
| Course     | `PATCH` | `/courses/{courseId}/close`           | `X-User-Id` | `CREATOR`    |
| Enrollment | `POST`  | `/enrollments`                        | `X-User-Id` | `STUDENT`    |
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
  "title": "Spring Boot 입문",
  "description": "스프링 부트 기초 강의",
  "price": 50000,
  "capacity": 30,
  "startDate": "2026-05-01",
  "endDate": "2026-05-31"
}
```

Response `201 Created`

```json
{
  "courseId": 1
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
    "courseId": 2,
    "creatorId": 1,
    "creatorName": "김강사",
    "title": "MSA 설계",
    "price": 120000,
    "capacity": 20,
    "startDate": "2026-06-01",
    "endDate": "2026-06-30",
    "status": "OPEN"
  },
  {
    "courseId": 3,
    "creatorId": 1,
    "creatorName": "김강사",
    "title": "Spring Boot 입문",
    "price": 50000,
    "capacity": 30,
    "startDate": "2026-05-01",
    "endDate": "2026-05-31",
    "status": "DRAFT"
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
  "courseId": 2,
  "creatorId": 1,
  "creatorName": "김강사",
  "title": "MSA 설계",
  "description": "MSA 아키텍처 기초",
  "price": 120000,
  "capacity": 20,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30",
  "status": "OPEN",
  "currentEnrollmentCount": 3
}
```

### 2.4 강의 모집 시작

- Method: `PATCH`
- Path: `/courses/{courseId}/open`
- Header: `X-User-Id`
- Role: `CREATOR`
- Body: 없음

Response `200 OK`

```json
{
  "courseId": 1,
  "status": "OPEN"
}
```

### 2.5 강의 모집 마감

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
  "enrollmentId": 10,
  "courseId": 2,
  "status": "PENDING"
}
```

### 3.2 내 수강 신청 목록 조회

- Method: `GET`
- Path: `/enrollments/me`
- Header: `X-User-Id`
- Role: `STUDENT`

Response `200 OK`

```json
[
  {
    "enrollmentId": 11,
    "courseId": 2,
    "courseTitle": "MSA 설계",
    "coursePrice": 120000,
    "status": "CONFIRMED",
    "createdAt": "2026-04-29T03:00:00",
    "confirmedAt": "2026-04-29T03:10:00",
    "cancelledAt": null
  },
  {
    "enrollmentId": 10,
    "courseId": 1,
    "courseTitle": "Spring Boot 입문",
    "coursePrice": 50000,
    "status": "PENDING",
    "createdAt": "2026-04-29T02:30:00",
    "confirmedAt": null,
    "cancelledAt": null
  }
]
```

### 3.3 결제 확정 처리

- Method: `PATCH`
- Path: `/enrollments/{enrollmentId}/confirm`
- Header: `X-User-Id`
- Role: `STUDENT`
- Body: 없음

Response `200 OK`

```json
{
  "enrollmentId": 10,
  "courseId": 2,
  "status": "CONFIRMED"
}
```

### 3.4 수강 취소

- Method: `PATCH`
- Path: `/enrollments/{enrollmentId}/cancel`
- Header: `X-User-Id`
- Role: `STUDENT`
- Body: 없음

취소 규칙:

- `PENDING` 상태는 즉시 취소 가능
- `CONFIRMED` 상태는 결제 확정일 기준 7일 이내
- `CONFIRMED` 상태는 강의 시작일 전까지만 취소 가능

Response `200 OK`

```json
{
  "enrollmentId": 10,
  "courseId": 2,
  "status": "CANCELLED"
}
```
