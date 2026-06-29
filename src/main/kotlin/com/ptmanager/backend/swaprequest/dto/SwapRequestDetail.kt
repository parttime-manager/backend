package com.ptmanager.backend.swaprequest.dto

import com.ptmanager.backend.domain.SwapRequestStatus
import com.ptmanager.backend.shift.dto.ShiftResponse
import java.time.Instant

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
