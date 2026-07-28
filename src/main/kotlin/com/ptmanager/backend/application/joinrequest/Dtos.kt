package com.ptmanager.backend.application.joinrequest


import com.ptmanager.backend.domain.JoinRequestStatus
import jakarta.validation.constraints.NotBlank
import java.time.Instant

/** 매장 가입 신청. 신청자는 인증 토큰(principal)에서 결정한다. */
data class CreateJoinRequest(
    @field:NotBlank val inviteCode: String,
)

/** 가입 신청 승인/거절 결정. (API 명세의 DecisionRequest) */
data class DecisionRequest(
    val decision: Decision,
) {
    enum class Decision { APPROVE, REJECT }
}

data class UpdateJoinRequestStatus(
    val status: JoinRequestStatus,
)

/** 가입 신청 응답. 명세 JoinRequest 스키마대로 신청자 표시명을 포함한다. */
data class JoinRequestResponse(
    val id: Long?,
    val workplaceId: Long,
    val userId: Long,
    val userName: String?,
    val status: JoinRequestStatus,
    val createdAt: Instant?,
)
