package com.ptmanager.backend.user

// User 기능 패키지 (API 명세의 `User` 태그).
//
// 예정 엔드포인트:
// - PATCH  /api/users/me                        프로필 수정
// - GET    /api/users/me/notification-setting   알림 설정 조회
// - PATCH  /api/users/me/notification-setting   알림 설정 수정
// - POST   /api/users/me/device-tokens          FCM 디바이스 토큰 등록
// - DELETE /api/users/me/device-tokens/{token}  FCM 디바이스 토큰 삭제
//
// 예정 구성: UserController, UserService, NotificationSettingService,
//           DeviceTokenService, user/dto/...
// 엔티티(NotificationSetting, DeviceToken)와 리포지토리는 이미 존재한다.
//
// "내가 누구인지(me)" 식별은 JWT 인증 도입 후 SecurityContext에서 가져온다.
// (config/security 참고) — 그 전까지는 빈 플레이스홀더 패키지다.
