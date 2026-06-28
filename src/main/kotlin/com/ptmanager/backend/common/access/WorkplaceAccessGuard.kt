package com.ptmanager.backend.common.access

import com.ptmanager.backend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

/**
 * 멀티테넌트 접근 제어. "지금 로그인한 사용자가 해당 매장의 멤버인가"를 검증한다.
 * RBAC(@PreAuthorize, 역할)과 별개로, 다른 매장의 데이터에 접근하는 것을 막는다.
 *
 * 현재 사용자 식별은 SecurityContext(JwtAuthenticationFilter가 주입한 principal=userId)에서 가져온다.
 */
@Component
class WorkplaceAccessGuard(
    private val userRepository: UserRepository,
) {

    fun currentUserId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.")
        return authentication.principal as? Long
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.")
    }

    fun currentWorkplaceId(): Long {
        val user = userRepository.findById(currentUserId())
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다.") }
        return user.workplaceId
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "소속 매장이 없습니다.")
    }

    /** 현재 사용자가 해당 매장 소속이 아니면 403. */
    fun requireMemberOf(workplaceId: Long) {
        if (currentWorkplaceId() != workplaceId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "해당 매장에 접근 권한이 없습니다.")
        }
    }
}
