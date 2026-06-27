package com.ptmanager.backend.swaprequest.dto

import jakarta.validation.constraints.NotBlank

data class CreateSwapRequest(
    val requesterId: Long,
    val shiftId: Long,
    @field:NotBlank val reason: String,
)
