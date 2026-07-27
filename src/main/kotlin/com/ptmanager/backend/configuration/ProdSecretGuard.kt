package com.ptmanager.backend.configuration

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * prod 기동 시 개발 기본 비밀값이 남아 있으면 실패시킨다.
 * (application.yml 기본값은 모두 `dev-only-` 접두사를 가진다)
 */
@Configuration
@Profile("prod")
class ProdSecretGuard(
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${qr.secret}") private val qrSecret: String,
) {

    @PostConstruct
    fun verify() {
        check(!jwtSecret.startsWith(DEV_PREFIX)) { "JWT_SECRET을 설정하세요. 개발 기본값으로는 prod 기동할 수 없습니다." }
        check(!qrSecret.startsWith(DEV_PREFIX)) { "QR_SECRET을 설정하세요. 개발 기본값으로는 prod 기동할 수 없습니다." }
        check(jwtSecret.toByteArray().size >= MIN_JWT_BYTES) { "JWT_SECRET은 최소 ${MIN_JWT_BYTES}바이트여야 합니다(HS256)." }
        check(jwtSecret != qrSecret) { "JWT_SECRET과 QR_SECRET은 서로 다른 값이어야 합니다." }
    }

    companion object {
        private const val DEV_PREFIX = "dev-only-"
        private const val MIN_JWT_BYTES = 32
    }
}
