# PTManager Backend

PTManager 직원/사장 안드로이드 앱을 위한 Spring Boot(Java 21) REST API.
알바 스케줄 관리 — 근무, 대타 교대, 매장 가입·승인, 공지, 알림을 다룬다.

## 실행

```bash
./gradlew bootRun
```

| 환경 | URL |
|------|-----|
| 로컬 | `http://localhost:8080` |
| 안드로이드 에뮬레이터 | `http://10.0.2.2:8080` |
| H2 콘솔 | `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:ptmanager`, 사용자 `sa`) |

## 동작 확인

```bash
./gradlew test
curl http://localhost:8080/api/health

# 시드 계정으로 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"employee@ptmanager.test","password":"password"}'
```

## 아키텍처

- 계층 구조: 컨트롤러 → 서비스 → Spring Data 리포지토리.
- 영속화: **Spring Data JPA + 임베디드 H2** (`MODE=PostgreSQL`). 인메모리
  데이터소스라 재시작마다 초기화되며 `config/DataSeeder`가 시드 데이터를 다시
  적재한다. 도메인 타입은 JPA `@Entity` 클래스이고 연관관계 대신 plain `Long`
  외래키 컬럼을 써서 평면 JSON으로 직렬화된다.
- PostgreSQL로 교체하려면 데이터소스만 재정의(예: `SPRING_DATASOURCE_URL`)하고
  `org.postgresql:postgresql` 드라이버를 추가하면 된다 — 엔티티는 DB 비종속이다.
  `src/main/resources/application.yml` 참고.
- 모든 에러는 `common/ApiExceptionHandler`를 통해 단일 `ApiError` 구조
  (`code`, `message`, `timestamp`, `fields`)로 반환된다:
  `400 BAD_REQUEST` / `400 VALIDATION_FAILED` / `404 NOT_FOUND`.

## API

전체 명세는 [API_SPEC.md](API_SPEC.md), 데이터 모델은 [ERD.md](ERD.md) 참고. 총 18개 엔드포인트:

- 인증 / 헬스: `GET /api/health`, `POST /api/auth/login`
- 매장: `GET|POST /api/workplaces`, `GET /api/workplaces/{id}/labor-cost?from=&to=`
- 근무: `GET /api/shifts?userId=`, `POST /api/shifts/{id}/check-in`
- 대타 요청: `GET|POST /api/swap-requests`, `PATCH /api/swap-requests/{id}/status`
- 가입 신청: `GET|POST /api/join-requests`, `PATCH /api/join-requests/{id}/status`
- 공지: `GET|POST /api/notices`, `GET /api/notices/{id}`
- 알림: `GET /api/notifications?userId=`, `PATCH /api/notifications/{id}/read`

## 시드 데이터

부팅 시 사용자 테이블이 비어 있으면 적재된다 (`config/DataSeeder`):

| 계정 | 역할 | 비밀번호 | 매장 (초대코드) |
|------|------|----------|------------------|
| `employee@ptmanager.test` | EMPLOYEE | `password` | PT Manager Cafe (`CAFE01`) |
| `employer@ptmanager.test` | EMPLOYER | `password` | PT Manager Cafe (`CAFE01`) |

직원 계정에는 시드 근무 2건(오늘·내일)이 함께 들어간다.

> **인증은 데모 스텁이다:** 비밀번호가 `password`로 하드코딩되어 있고 발급되는
> `dev-token-{id}`는 검증되지 않으며, 아직 역할 기반 권한도 없다.
> JWT + 권한 처리가 다음 단계다.
