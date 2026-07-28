package com.ptmanager.backend.application.joinrequest


import com.ptmanager.backend.common.BusinessError
import com.ptmanager.backend.common.BusinessException
import com.ptmanager.backend.common.access.WorkplaceAccessGuard
import com.ptmanager.backend.common.nameOf
import com.ptmanager.backend.common.namesOf
import com.ptmanager.backend.common.orNotFound
import com.ptmanager.backend.domain.JoinRequest
import com.ptmanager.backend.domain.JoinRequestStatus
import com.ptmanager.backend.domain.NotificationType
import com.ptmanager.backend.application.notification.NotificationService
import com.ptmanager.backend.application.port.out.JoinRequestRepository
import com.ptmanager.backend.application.port.out.UserRepository
import com.ptmanager.backend.application.port.out.WorkplaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

@Service
class JoinRequestService(
    private val joinRequestRepository: JoinRequestRepository,
    private val workplaceRepository: WorkplaceRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    private val accessGuard: WorkplaceAccessGuard,
) {

    fun findByWorkplace(workplaceId: Long, status: JoinRequestStatus): List<JoinRequestResponse> {
        accessGuard.requireMemberOf(workplaceId)
        val requests = joinRequestRepository.findByWorkplaceIdAndStatusOrderByCreatedAtDesc(workplaceId, status)
        val names = userRepository.namesOf(requests.map { it.userId })
        return requests.map { toResponse(it, names[it.userId]) }
    }

    @Transactional
    fun create(inviteCode: String, userId: Long): JoinRequestResponse {
        val workplace = workplaceRepository.findByInviteCode(inviteCode)
            ?: throw NoSuchElementException("Invalid invite code.")
        if (!userRepository.existsById(userId)) {
            throw NoSuchElementException("User not found.")
        }
        // 같은 매장에 처리 대기(PENDING) 신청이 이미 있으면 중복 차단
        if (joinRequestRepository.existsByWorkplaceIdAndUserIdAndStatus(
                workplace.id!!, userId, JoinRequestStatus.PENDING,
            )
        ) {
            throw BusinessException(BusinessError.STATE_CONFLICT, "이미 처리 대기 중인 가입 신청이 있습니다.")
        }
        val saved = joinRequestRepository.save(
            JoinRequest(
                workplaceId = workplace.id!!,
                userId = userId,
                status = JoinRequestStatus.PENDING,
            ),
        )
        return toResponse(saved, userRepository.nameOf(userId))
    }

    @Transactional
    fun updateStatus(id: Long, status: JoinRequestStatus): JoinRequestResponse {
        val request = joinRequestRepository.findById(id)
            .orNotFound("Join request not found.")
        accessGuard.requireMemberOf(request.workplaceId)

        // 원자적 가드: PENDING일 때만 전이 (영향 행 0이면 이미 처리됨)
        val updated = joinRequestRepository.markStatus(id, status, JoinRequestStatus.PENDING)
        if (updated == 0) {
            throw BusinessException(BusinessError.STATE_CONFLICT, "이미 처리된 가입 신청입니다.")
        }
        // markStatus가 영속성 컨텍스트를 clear 하므로 재조회
        val saved = joinRequestRepository.findById(id)
            .orNotFound("Join request not found.")

        if (status == JoinRequestStatus.APPROVED) {
            val user = userRepository.findById(saved.userId)
                .orNotFound("User not found.")
            user.workplaceId = saved.workplaceId
        }
        notificationService.notify(
            saved.userId,
            NotificationType.JOIN_REQUEST,
            "매장 가입 신청이 ${status.name} 처리되었습니다.",
            targetType = "JOIN_REQUEST",
            targetId = saved.id,
        )
        return toResponse(saved, userRepository.nameOf(saved.userId))
    }

    private fun toResponse(request: JoinRequest, userName: String?): JoinRequestResponse =
        JoinRequestResponse(
            id = request.id,
            workplaceId = request.workplaceId,
            userId = request.userId,
            userName = userName,
            status = request.status,
            createdAt = request.createdAt,
        )
}
