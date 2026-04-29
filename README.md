## 1. 프로젝트 개요

크리에이터(강사)와 클래스메이트(수강생) 간의 수강 신청 흐름을 처리하는 백엔드 API 서버입니다.
강의 개설부터 수강 신청, 결제 확정, 수강 취소 흐름을 REST API로 제공하며, 동시 신청 상황에서의 정원 초과를 방지하기 위한 동시성 제어를 포함합니다.

개발기간 : 2026.04.24 - 2026.04.29

**주요 기능**

- 강의(`Course`) 관리 및 상태 전이 (`DRAFT` → `OPEN` → `CLOSED`)
- 수강 신청(`Enrollment`) 관리 및 상태 전이 (`PENDING`, `WAITING`, `CONFIRMED`, `CANCELLED`)
- 정원 초과 방지 및 동시 신청에 대한 동시성 제어

## 2. 기술 스택

Language: Java 17

Framework: Spring Boot 3.5

Persistence: Spring Data JPA, MySQL

Test: JUnit 5, H2

Build: Gradle

Etc: Docker Compose Support

## 3. 프로젝트 실행 / 테스트 실행 방법

### [실행]

샘플 데이터가 필요한 경우에만 `init` 프로필을 활성화해 실행합니다.

일반 실행에는 필요하지 않으며, 초기 데이터 적재가 필요할 때만 사용합니다.

기존 데이터가 이미 있는 상태에서 `init` 프로필을 포함하여 실행하면 데이터가 삭제되고 샘플 데이터로 대체됩니다.

**[애플리케이션 실행]**

사전 환경 구성 : Java 17, Docker
API 호출 편의성을 위해 swagger를 추가했습니다. 애플리케이션 실행 후 다음 url 을 통해 사용할 수 있습니다.
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

```bash
# Linux/Mac
./gradlew bootRun --args='--spring.profiles.active=init' # with init profile
./gradlew bootRun # default

# Windows
gradlew.bat bootRun --args="--spring.profiles.active=init" # with init profile
gradlew.bat bootRun # default
```

**[테스트 실행]**

테스트는 H2 기반으로 실행되어 별도의 사전 구성 없이 수행할 수 있습니다.

```bash
# 전체 테스트 실행
./gradlew cleanTest test
gradlew.bat cleanTest test

# 특정 테스트 클래스 실행 예시
./gradlew test --tests "leehs.course.core.enrollment.application.EnrollmentApplierConcurrencyTest"
gradlew.bat test --tests "leehs.course.core.enrollment.application.EnrollmentApplierConcurrencyTest"
```

## 4. 요구사항 해석 및 가정

### [공통]

- 인증/인가는 단순화를 위해 요청 헤더(`X-User-Id`)로 사용자를 식별하고, Service 레이어에서 역할 및 소유권 검증을 수행하도록 구현했습니다.

### [강의 관리]

- 강의 상태 전이는 요구사항의 흐름을 따라 `DRAFT` -> `OPEN` -> `CLOSED`의 단방향 전이로 해석했습니다.
- 강의 상태와 정원 상태는 별개로 보았습니다. 따라서 강의의 수강 신청 인원이 정원에 도달하는 경우 자동으로 `OPEN` 상태에서 `CLOSED` 상태로 변경하지 않았습니다.
- 강의별 수강생 목록 조회는 수강 신청 상태가 `CONFIRMED`인 경우만 집계하고 조회하도록 구현했습니다.

### [수강 신청 관리]

- 정원을 차지하는 수강 신청 건은 `PENDING`, `CONFIRMED`만 포함하고 `CANCELLED`는 제외한다고 해석했습니다.
- 취소된 신청이 있으면 동일 강의에 다시 신청할 수 있도록 구현했습니다.
- 수강 취소는 `PENDING`, `WAITING` 상태에서 가능하며, `CONFIRMED` 상태에서는 수강 확정일 기준 7일 이내, 강의 시작일 전인 경우에 허용하도록 구현했습니다.
- 정원 초과 시 수강 신청이 거부되며, 사용자가 별도로 대기열 등록을 요청한 경우에만 `WAITING` 상태의 수강 신청 건을 생성하도록 구현했습니다.
- 대기열 등록은 정원이 가득 찬 경우에만 허용하고, `PENDING` 혹은 `CONFIRMED` 상태의 수강 신청이 취소되면 가장 오래된 대기열 등록을 `PENDING` 상태로 변경되도록 구현했습니다.

## 5. 설계 결정과 이유

### [프로젝트 구조]

- 주요 도메인은 `User`, `Course`, `Enrollment` 세 가지로 나누고, 각 도메인 내부는 `api`, `application`, `domain` 계층으로 구성했습니다.
- `api`: 요청 매핑, 입력 검증, 현재 사용자 식별, 응답 DTO 변환을 담당합니다.
- `application`: 유스케이스 조합, 역할/소유권 검증, 상태 전이, 정원 검증 등 비즈니스 흐름을 담당합니다.
- `domain`: 엔티티, enum, repository 인터페이스, 도메인 예외 등 핵심 모델과 규칙을 담당합니다.

- `global` 패키지는 공통 예외 처리, 인증/인가 관련 어노테이션, Swagger 설정이 정의되어있습니다.

### [테스트]

- `domain` 테스트는 순수 단위 테스트로 작성해 상태 전이와 도메인 규칙 자체를 빠르게 검증했습니다.
- `application` 테스트는 Spring Boot 통합 테스트로 작성해 서비스 계층의 비즈니스 규칙, 트랜잭션, 데이터베이스와 협력을 검증했습니다.
- `api` 테스트는 MockMvc 기반 테스트로 작성해 요청/응답 구조, 헤더 처리, 검증 오류, HTTP 상태 코드를 검증했습니다.
- 테스트 메서드 네이밍은 `when (행위) expect (기대결과)` 패턴으로 통일했습니다.

### [비관적 락을 통한 동시성 제어]

- 문제 상황 1
  - 마지막 정원에 여러 사용자가 동시에 수강 신청하면 활성 수강 신청 인원이 정원을 초과할 수 있습니다.
- 문제 상황 2
  - 수강 취소와 대기열 등록이 동시에 발생하면 빈 자리가 남았는데도 `WAITING` 상태가 생성되거나 남을 수 있습니다.
- 해결 방법
  - 강의별 정원과 대기열 상태를 하나의 공유 자원으로 보고, 수강 신청 등록/수강 신청 취소/대기열 등록 시 강의 조회에 비관적 락을 적용했습니다.
  - 별도 통합 테스트인 [EnrollmentApplierConcurrencyTest](src/test/java/leehs/course/core/enrollment/application/EnrollmentApplierConcurrencyTest.java)로 마지막 정원 신청 상황과 수강 취소/대기열 등록 동시 상황을 검증했습니다.

### [RFC 7807 기반 예외 처리]

- 예외 응답은 Spring `ProblemDetail`을 사용해 RFC 7807 형식으로 통일했습니다.
- 각 도메인 예외는 에러 코드와 HTTP 상태 코드를 명시하고`ApplicationException`을 상속받아, `GlobalExceptionHandler`에서 이를 `ProblemDetail`로 변환해 일관된 응답 구조를 반환합니다.

## 6. 구현 사항

### [필수 구현]
1. 강의 관리
   - [x] 강의 등록
   - [x] 강의 상태 변경
   - [x] 강의 목록 조회 (상태 필터)
   - [x] 강의 상세 조회 (현재 신청 인원 포함)


2. 수강 신청 관리
   - [x] 수강 신청 (강의 최대 정원 초과 신청 시 거부, 마지막 자리 신청 동시성 적용)
   - [x] 결제 확정 처리
   - [x] 수강 취소
   - [x] 내 수강 신청 목록 조회

### [선택 구현]
- [x] 수강 취소 시 취소 가능 기간 제한 (결제 후 7일 이내, 강의 시작일 전까지만 허용)
- [x] 강의별 수강생 목록 조회 (크리에이터 전용)
- [x] 신청 내역 페이지네이션
- [x] 대기열(waitlist) 기능

### [미구현]

## 7. AI 활용 범위

- 코드 구현, 테스트 코드 작성, 문서 정리 과정에서 `codex`를 보조 도구로 활용했습니다.
- 가이드 문서인 [AGENTS.md](AGENTS.md)를 참고해 작업을 수행하도록 했습니다.
- 코드 리뷰 시 [code-review.md](skills/code-review.md)를 참고하도록 했습니다.

## 8. API 목록 및 예시

상세 API 명세는 [API명세서.md](docs/API명세서.md) 문서를 참고해주세요.

## 9. ERD / 데이터 모델 설명

ERD 및 데이터 모델 설명은 [데이터모델.md](docs/데이터모델.md) 문서를 참고해주세요.
