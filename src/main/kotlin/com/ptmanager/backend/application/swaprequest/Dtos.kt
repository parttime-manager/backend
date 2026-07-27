package com.ptmanager.backend.application.swaprequest


import com.ptmanager.backend.domain.SwapRequest
import com.ptmanager.backend.domain.SwapRequestStatus
import com.ptmanager.backend.application.shift.ShiftResponse
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

/** 대타 요청 생성. 요청자는 인증 토큰(principal)에서 결정한다. */
data class CreateSwapRequest(
    val shiftId: Long,
    @field:NotBlank @field:Size(max = 500) val reason: String,
)

data class ApproveSwapRequest(
    val applicantId: Long,
)

/** 대타 지원 응답. 명세 SwapApplication 스키마대로 지원자 표시명을 포함한다. */
data class SwapApplicationResponse(
    val id: Long?,
    val swapRequestId: Long,
    val applicantId: Long,
    val applicantName: String?,
    val status: SwapRequestStatus,
    val createdAt: Instant?,
)

/**
 * 대타 요청 상세 = SwapRequest 필드(평면) + 대상 근무 + 지원자 목록.
 * (명세의 SwapRequestDetail = allOf SwapRequest + { shift, applications })
 */
data class SwapRequestDetail(
    val id: Long?,
    val workplaceId: Long,
    val shiftId: Long,
    val requesterId: Long,
    val substituteId: Long?,
    val reason: String,
    val status: SwapRequestStatus,
    val createdAt: Instant?,
    val shift: ShiftResponse?,
    val applications: List<SwapApplicationResponse>,
)

data class SwapRequestResponse(
    val id: Long?,
    val workplaceId: Long,
    val shiftId: Long,
    val requesterId: Long,
    val substituteId: Long?,
    val reason: String,
    val status: SwapRequestStatus,
    val createdAt: Instant?,
) {
    companion object {
        fun from(entity: SwapRequest): SwapRequestResponse = SwapRequestResponse(
            id = entity.id,
            workplaceId = entity.workplaceId,
            shiftId = entity.shiftId,
            requesterId = entity.requesterId,
            substituteId = entity.substituteId,
            reason = entity.reason,
            status = entity.status,
            createdAt = entity.createdAt,
        )
    }
}
