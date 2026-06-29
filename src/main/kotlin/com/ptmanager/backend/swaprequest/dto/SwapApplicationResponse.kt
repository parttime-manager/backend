package com.ptmanager.backend.swaprequest.dto

import com.ptmanager.backend.domain.SwapRequestStatus
import java.time.Instant

/** 대타 지원 응답. 명세 SwapApplication 스키마대로 지원자 표시명을 포함한다. */
data class SwapApplicationResponse(
    val id: Long?,
    val swapRequestId: Long,
    val applicantId: Long,
    val applicantName: String?,
    val status: SwapRequestStatus,
    val createdAt: Instant?,
)
