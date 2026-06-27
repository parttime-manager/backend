package com.ptmanager.backend.swaprequest.dto

import com.ptmanager.backend.domain.SwapRequestStatus

data class UpdateSwapRequestStatus(
    val status: SwapRequestStatus,
)
