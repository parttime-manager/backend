package com.ptmanager.backend.config.security

// JWT 인증 / Spring Security 설정이 위치할 패키지.
//
// 예정 구성:
// - SecurityConfig          SecurityFilterChain, PasswordEncoder(BCrypt) 빈,
//                           공개 경로(/api/auth, /api/health) 화이트리스트
// - JwtTokenProvider        액세스/리프레시 토큰 발급·검증
// - JwtAuthenticationFilter Authorization Bearer 헤더 파싱 → SecurityContext 주입
//
// 도입 시 build.gradle.kts 에 spring-boot-starter-security + JWT 라이브러리
// (예: io.jsonwebtoken:jjwt) 추가가 필요하다.
// 현재는 미도입 상태의 플레이스홀더 패키지다.
