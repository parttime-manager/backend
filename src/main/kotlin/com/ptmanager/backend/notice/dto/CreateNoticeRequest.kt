package com.ptmanager.backend.notice.dto

import jakarta.validation.constraints.NotBlank

data class CreateNoticeRequest(
    val workplaceId: Long,
    val authorId: Long,
    @field:NotBlank val title: String,
    @field:NotBlank val body: String,
)
