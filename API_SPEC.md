# PTManager Backend API 명세서

Spring Boot REST API. 영속화는 Spring Data JPA + H2(임베디드)로 구현되어 있다. 기본 프로파일은 인메모리 H2(`jdbc:h2:mem`)라 재시작 시 데이터가 초기화되고 부팅 시 시드가 다시 적재된다. PostgreSQL로 교체하려면 datasource만 바꾸면 된다.

## 공통 사항

| 항목 | 값 |
|------|----|
| Base URL | `http://localhost:8080` (에뮬레이터에서는 `http://10.0.2.2:8080`) |
| 포맷 | `application/json` (요청·응답 모두) |
| 인증 | 로그인 시 `dev-token-{userId}` 발급(데모용, 검증 미적용) |
| 날짜/시간 | `LocalDate` = `YYYY-MM-DD`, `LocalTime` = `HH:mm:ss`, `Instant` = ISO-8601 UTC |

### 시드 데이터 (`DataSeeder`)
부팅 시 사용자 테이블이 비어 있으면 아래 데이터가 적재된다.

- Workplace: `{ id: 1, name: "PT Manager Cafe", address: "Seoul Gangnam-gu", inviteCode: "CAFE01" }`
- User 1: `employee@ptmanager.test` / `Kim Employee` / `EMPLOYEE` / workplaceId 1 / hourlyWage 12000
- User 2: `employer@ptmanager.test` / `Park Employer` / `EMPLOYER` / workplaceId 1 / hourlyWage 0
- Shift 1·2: userId 1(직원)의 오늘·내일 근무
- 모든 로그인 비밀번호는 `password` (데모용 하드코딩)

### 공통 에러 응답
모든 에러는 동일한 `ApiError` 구조를 반환한다.

```json
{
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed.",
  "timestamp": "2026-06-25T09:00:00Z",
  "fields": { "reason": "공백일 수 없습니다" }
}
```

| HTTP | code | 발생 조건 |
|------|------|-----------|
| 400 | `BAD_REQUEST` | 잘못된 인자(비밀번호 불일치, 중복 출근체크, 기간 역전 등) — `IllegalArgumentException` |
| 400 | `VALIDATION_FAILED` | `@Valid` 본문 검증 실패. `fields`에 필드별 메시지 |
| 404 | `NOT_FOUND` | 대상 미존재(사용자/매장/대타·가입요청/공지/알림, 잘못된 초대코드) — `NoSuchElementException` |

---

## 엔드포인트

### 1. 헬스 체크 — `GET /api/health`
응답 `200 OK`
```json
{ "status": "UP", "timestamp": "2026-06-25T09:00:00Z" }
```

### 2. 로그인 — `POST /api/auth/login`
요청
```json
{ "email": "employee@ptmanager.test", "password": "password" }
```
응답 `200 OK`
```json
{
  "accessToken": "dev-token-1",
  "user": { "id": 1, "email": "employee@ptmanager.test", "name": "Kim Employee", "role": "EMPLOYEE", "workplaceId": 1, "hourlyWage": 12000 }
}
```
오류: `400 BAD_REQUEST`(비밀번호 ≠ `password`), `404 NOT_FOUND`(없는 이메일), `400 VALIDATION_FAILED`.

### 3. 매장 목록 — `GET /api/workplaces`
응답 `200 OK`
```json
[ { "id": 1, "name": "PT Manager Cafe", "address": "Seoul Gangnam-gu", "inviteCode": "CAFE01" } ]
```

### 4. 매장 생성 — `POST /api/workplaces`
사장이 매장을 만들면 고유 6자리 `inviteCode`가 발급된다.

요청
| 필드 | 타입 | 필수 |
|------|------|------|
| `name` | string | ✅ (공백 불가) |
| `address` | string | ❌ |
```json
{ "name": "Second Store", "address": "Busan" }
```
응답 `201 Created`
```json
{ "id": 2, "name": "Second Store", "address": "Busan", "inviteCode": "QS4FDX" }
```

### 5. 인건비 조회 — `GET /api/workplaces/{id}/labor-cost?from={date}&to={date}`
기간 내 매장의 근무를 직원별로 집계해 인건비를 계산한다. `cost = totalMinutes × hourlyWage ÷ 60`.

응답 `200 OK`
```json
{
  "workplaceId": 1,
  "from": "2026-06-01",
  "to": "2026-06-30",
  "totalCost": 132000,
  "employees": [
    { "employeeId": 1, "name": "Kim Employee", "totalMinutes": 660, "hourlyWage": 12000, "cost": 132000 }
  ]
}
```
오류: `404 NOT_FOUND`(없는 매장), `400 BAD_REQUEST`(`to` < `from`).

### 6. 근무 조회 — `GET /api/shifts?userId={userId}`
직원의 근무 목록(`workDate`, `startTime` 오름차순).

응답 `200 OK`
```json
[ { "id": 1, "workplaceId": 1, "employeeId": 1, "workDate": "2026-06-25", "startTime": "09:00:00", "endTime": "14:00:00", "checkedIn": false } ]
```
오류: `404 NOT_FOUND`(없는 `userId`).

### 7. 출근 체크 — `POST /api/shifts/{id}/check-in`
해당 근무의 `checkedIn`을 true로 변경.

응답 `200 OK` — 변경된 `Shift`
오류: `404 NOT_FOUND`(없는 근무), `400 BAD_REQUEST`(이미 출근체크됨).

### 8. 대타 요청 목록 — `GET /api/swap-requests`
전체 대타 요청을 `createdAt` 내림차순(최신순)으로 반환.

응답 `200 OK`
```json
[ { "id": 1, "workplaceId": 1, "requesterId": 1, "substituteId": null, "workDate": "2026-06-26", "reason": "Doctor appointment", "status": "PENDING", "createdAt": "2026-06-25T09:00:00Z" } ]
```

### 9. 대타 요청 생성 — `POST /api/swap-requests`
`workplaceId`는 요청자 매장으로 자동 설정, `status`는 `PENDING`으로 시작.

요청
| 필드 | 타입 | 필수 |
|------|------|------|
| `requesterId` | long | ✅ |
| `substituteId` | long | ❌ |
| `workDate` | date | ✅ |
| `reason` | string | ✅ (공백 불가) |
```json
{ "requesterId": 1, "substituteId": null, "workDate": "2026-06-26", "reason": "Doctor appointment" }
```
응답 `200 OK` — 생성된 `SwapRequest`.
오류: `404 NOT_FOUND`(없는 requester/substitute), `400 VALIDATION_FAILED`.

### 10. 대타 상태 변경 — `PATCH /api/swap-requests/{id}/status`
승인/거절. 처리 시 요청자에게 `SWAP_RESULT` 알림이 생성된다.

요청 `{ "status": "APPROVED" }` (`PENDING`|`APPROVED`|`REJECTED`)
응답 `200 OK` — 변경된 `SwapRequest`.
오류: `404 NOT_FOUND`, `400 VALIDATION_FAILED`.

### 11. 가입 신청 목록 — `GET /api/join-requests?workplaceId={id}`
매장의 가입 신청을 최신순으로 반환.

응답 `200 OK`
```json
[ { "id": 1, "workplaceId": 2, "userId": 1, "status": "PENDING", "createdAt": "2026-06-25T09:00:00Z" } ]
```

### 12. 가입 신청 — `POST /api/join-requests`
직원이 초대코드로 매장 가입을 신청한다.

요청
| 필드 | 타입 | 필수 |
|------|------|------|
| `inviteCode` | string | ✅ (공백 불가) |
| `userId` | long | ✅ |
```json
{ "inviteCode": "QS4FDX", "userId": 1 }
```
응답 `201 Created` — `PENDING` 상태의 `JoinRequest`.
오류: `404 NOT_FOUND`(잘못된 초대코드 / 없는 사용자).

### 13. 가입 승인·거절 — `PATCH /api/join-requests/{id}/status`
`APPROVED` 시 해당 사용자의 `workplaceId`가 매장으로 배정되고, 신청자에게 `JOIN_REQUEST` 알림이 생성된다.

요청 `{ "status": "APPROVED" }` (`PENDING`|`APPROVED`|`REJECTED`)
응답 `200 OK` — 변경된 `JoinRequest`.
오류: `404 NOT_FOUND`, `400 VALIDATION_FAILED`.

### 14. 공지 목록 — `GET /api/notices?workplaceId={id}`
매장 공지를 최신순으로 반환. 응답 `200 OK` — `Notice[]`.

### 15. 공지 상세 — `GET /api/notices/{id}`
응답 `200 OK` — `Notice`. 오류: `404 NOT_FOUND`.

### 16. 공지 작성 — `POST /api/notices`
요청
| 필드 | 타입 | 필수 |
|------|------|------|
| `workplaceId` | long | ✅ |
| `authorId` | long | ✅ |
| `title` | string | ✅ (공백 불가) |
| `body` | string | ✅ (공백 불가) |
```json
{ "workplaceId": 1, "authorId": 2, "title": "7월 스케줄 공지", "body": "7월 1일부터 변경됩니다." }
```
응답 `201 Created` — 생성된 `Notice`.

### 17. 알림 목록 — `GET /api/notifications?userId={id}`
사용자 알림을 최신순으로 반환.

응답 `200 OK`
```json
[ { "id": 1, "userId": 1, "type": "JOIN_REQUEST", "message": "매장 가입 신청이 APPROVED 처리되었습니다.", "read": false, "createdAt": "2026-06-25T09:00:00Z" } ]
```
`type`: `JOIN_REQUEST` | `SWAP_REQUEST` | `SWAP_RESULT` | `ATTENDANCE` | `NOTICE`.

### 18. 알림 읽음 처리 — `PATCH /api/notifications/{id}/read`
응답 `200 OK` — `read: true`로 변경된 `Notification`. 오류: `404 NOT_FOUND`.

---

## 엔드포인트 요약

| # | Method | Path | 설명 | 성공 |
|---|--------|------|------|------|
| 1 | GET | `/api/health` | 헬스 체크 | 200 |
| 2 | POST | `/api/auth/login` | 로그인 | 200 |
| 3 | GET | `/api/workplaces` | 매장 목록 | 200 |
| 4 | POST | `/api/workplaces` | 매장 생성(+초대코드) | 201 |
| 5 | GET | `/api/workplaces/{id}/labor-cost` | 인건비 집계 | 200 |
| 6 | GET | `/api/shifts?userId=` | 직원 근무 조회 | 200 |
| 7 | POST | `/api/shifts/{id}/check-in` | 출근 체크 | 200 |
| 8 | GET | `/api/swap-requests` | 대타 목록 | 200 |
| 9 | POST | `/api/swap-requests` | 대타 생성 | 200 |
| 10 | PATCH | `/api/swap-requests/{id}/status` | 대타 승인/거절 | 200 |
| 11 | GET | `/api/join-requests?workplaceId=` | 가입신청 목록 | 200 |
| 12 | POST | `/api/join-requests` | 초대코드로 가입신청 | 201 |
| 13 | PATCH | `/api/join-requests/{id}/status` | 가입 승인/거절 | 200 |
| 14 | GET | `/api/notices?workplaceId=` | 공지 목록 | 200 |
| 15 | GET | `/api/notices/{id}` | 공지 상세 | 200 |
| 16 | POST | `/api/notices` | 공지 작성 | 201 |
| 17 | GET | `/api/notifications?userId=` | 알림 목록 | 200 |
| 18 | PATCH | `/api/notifications/{id}/read` | 알림 읽음 | 200 |

> 데이터 모델 상세는 [ERD.md](ERD.md) 참고. 토큰 검증·역할 기반 권한은 미구현(데모 단계)이며, 비밀번호는 `password`로 하드코딩되어 있다.
