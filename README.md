# PTManager Backend

직원·사장 Android 앱이 근무 스케줄·대타·근태·매장 데이터를 공유하는 REST API 서버입니다.

## 기술 스택

Kotlin · Spring Boot · Spring Data JPA · Spring Security · H2 / PostgreSQL · Flyway · JWT · FCM · S3

의존성은 [build.gradle.kts](build.gradle.kts)에서 관리합니다.

## 시작하기

### 사전 요구사항

JDK 21이 필요합니다. `JAVA_HOME`이 해당 JDK를 가리키도록 설정합니다. Android Studio의 번들 JBR도 사용할 수 있습니다.

### 실행

저장소 루트에서 실행합니다.

```bash
./gradlew bootRun
```

Windows PowerShell에서는 `./gradlew` 대신 `.\gradlew`를 사용합니다.
로컬 설정은 H2 인메모리 DB를 사용하므로 별도 DB 설치가 필요하지 않습니다.

## 사용 방법

### 로컬 접속

| 대상 | 주소 |
| --- | --- |
| API | `http://localhost:8080` |
| Android 에뮬레이터에서 API | `http://10.0.2.2:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI | `http://localhost:8080/v3/api-docs` |
| H2 콘솔 | `http://localhost:8080/h2-console` |

H2 콘솔의 JDBC URL은 `jdbc:h2:mem:ptmanager`, 사용자는 `sa`입니다.
보호된 API는 로그인으로 받은 `accessToken`을 Swagger의 **Authorize**에 입력해 호출합니다.

```bash
curl http://localhost:8080/actuator/health
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"employee@ptmanager.test","password":"password"}'
```

### 데모 계정

로컬 DB는 재시작할 때 초기화됩니다. 사용자 테이블이 비어 있으면 [DataSeeder](src/main/kotlin/com/ptmanager/backend/config/DataSeeder.kt)가 샘플을 생성합니다.
비밀번호는 모두 `password`, 시드 매장 초대코드는 `CAFE01`입니다.

| 계정 | 역할 | 매장 |
| --- | --- | --- |
| `employer@ptmanager.test` | 사장 | PT Manager Cafe |
| `employee@ptmanager.test` | 직원 | PT Manager Cafe |
| `employee2@ptmanager.test` | 직원 | PT Manager Cafe |
| `employee3@ptmanager.test` | 직원 | PT Manager Cafe |
| `applicant@ptmanager.test` | 직원 | 미소속·가입 신청 대기 |

근무·근태·공지·인수인계·대타·알림 샘플도 함께 생성됩니다. 샘플 구성은 `DataSeeder`에서 관리합니다.

## 테스트

```bash
./gradlew test
```

자동 검증 설정은 [CI 워크플로](.github/workflows/ci.yml)를 참고합니다.

## 관련 문서

| 문서 | 내용 |
| --- | --- |
| [프로젝트 문서](https://github.com/PTManager/docs#관련-문서) | 요구사항·설계·작업 현황·배포 안내 |
| [직원 앱](https://github.com/PTManager/EmployeeApp) | 직원 앱 설정·빌드·사용 |
| [사장 앱](https://github.com/PTManager/EmployerApp) | 사장 앱 설정·빌드·사용 |
