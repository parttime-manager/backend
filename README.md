# PTManager Backend

## 소개

직원·사장 Android 앱이 근무 스케줄·대타·근태·매장 데이터를 공유하는 REST API 서버입니다.

## 기술 스택

Kotlin · Spring Boot · Spring Data JPA · Spring Security · H2 / PostgreSQL · Flyway · JWT · FCM · S3 ([build.gradle.kts](build.gradle.kts))

### 아키텍처

헥사고날·클린 아키텍처를 사용하며 경계는 [기술 구조](https://github.com/parttime-manager/.github/blob/main/03-tech/01-architecture.md#아키텍처와-경계)를 따릅니다.

## 시작하기

JDK 21(`JAVA_HOME`)을 준비하고 저장소 루트에서 실행합니다(Windows는 `.\gradlew`). 기본 로컬 설정은 H2 인메모리 DB를 사용하며 운영 설정은 [배포 가이드](https://github.com/parttime-manager/.github/blob/main/05-ops/01-deploy.md)를 따릅니다.

```bash
./gradlew bootRun
```

## 사용 방법

| 대상 | 주소 | 비고 |
| --- | --- | --- |
| API / 에뮬레이터 | `http://localhost:8080` / `http://10.0.2.2:8080` | [bruno](bruno) 컬렉션 지원 |
| Scalar / OpenAPI | `http://localhost:8080/docs` / `/v3/api-docs` | Bearer `accessToken` 입력 |
| H2 콘솔 | `http://localhost:8080/h2-console` | `jdbc:h2:mem:ptmanager`, 사용자 `sa`, 비밀번호 빈 값 |

```bash
curl http://localhost:8080/actuator/health
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"employee@ptmanager.test","password":"password"}'
```

### 데모 계정

로컬 기동 시 사용자 테이블이 비어 있으면 [DataSeeder](src/main/kotlin/com/ptmanager/backend/configuration/DataSeeder.kt)가 샘플 데이터를 생성합니다(비밀번호 공통 `password`, 초대코드 `CAFE01`).

| 계정 | 역할 | 매장 |
| --- | --- | --- |
| `employer@ptmanager.test` | 사장 | PT Manager Cafe |
| `employee@ptmanager.test`, `employee2@...`, `employee3@...` | 직원 | PT Manager Cafe |
| `applicant@ptmanager.test` | 직원 | 미소속·가입 신청 대기 |

## 테스트

```bash
./gradlew test
```

CI 설정은 [CI 워크플로](.github/workflows/ci.yml)를 따릅니다.

## 관련 문서

- [프로젝트 문서](https://github.com/parttime-manager/.github#관련-문서) · [업무 규칙](https://github.com/parttime-manager/.github/blob/main/01-func/01-requirements.md#업무-규칙) · [데이터 모델](https://github.com/parttime-manager/.github/blob/main/03-tech/02-data-model.md) · [Android](https://github.com/parttime-manager/android-app)
