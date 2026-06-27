package com.ptmanager.backend.joinrequest.dto

import jakarta.validation.constraints.NotBlank

data class CreateJoinRequest(
    @field:NotBlank val inviteCode: String,
    val userId: Long,
)
